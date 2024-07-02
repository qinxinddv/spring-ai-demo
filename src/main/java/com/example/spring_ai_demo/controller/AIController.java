package com.example.spring_ai_demo.controller;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AIController {
    private final OllamaChatModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    @Autowired
    public AIController(OllamaChatModel chatModel, EmbeddingModel embeddingModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }
    @CrossOrigin
    @GetMapping("/ai/generate")
    public String generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return chatModel.call(message);
    }

    @CrossOrigin
    @GetMapping("/ai/generateStream")
    public Flux<String> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Content-Type","text/event-stream");
        Mono<Prompt> promptMono = exchange.getSession().flatMap(webSession -> {
            Prompt prompt = webSession.getAttribute("prompt");
            if (prompt == null) {
                prompt = new Prompt(message);
            }else {
                List<Message> messages = new ArrayList<>();
                messages.add(new UserMessage(prompt.getContents()));
                messages.add(new UserMessage(message));
                prompt = new Prompt(messages);
            }
            webSession.getAttributes().put("prompt",prompt);
            return Mono.just(prompt);
        });
        Flux<String> stream = promptMono.flatMapMany(this::getReply);

        return stream;
    }
    private Flux<String> getReply(Prompt prompt){
        return chatModel.stream(prompt.getContents());
    }

    @GetMapping("/ai/query")
    public Flux<String> query(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        // 使用自然语言查询 VectorStore，查找有关个人的信息。
        List<Document> similarDocuments =
                vectorStore.similaritySearch(SearchRequest.query(message).withTopK(2));
        String information = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));
        System.out.println(information);
        var systemPromptTemplate = new SystemPromptTemplate("""
              现在的时间是{date}
              你需要使用文档内容对用户提出的问题进行回复，同时你需要表现得天生就知道这些内容，
              不能在回复中体现出你是根据给出的文档内容进行回复的，这点非常重要。
              当用户提出的问题无法根据文档内容进行回复或者你也不清楚时，回复不知道即可。
              文档内容如下:
              {information}
              """);
        Map<String, Object> map = new HashMap<>();
        map.put("information", information);
        map.put("date", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        var systemMessage = systemPromptTemplate.createMessage(
                map);
        var userMessage = new UserMessage(message);

        return chatModel.stream(new Prompt(List.of(systemMessage, userMessage)).getContents());
    }
}
