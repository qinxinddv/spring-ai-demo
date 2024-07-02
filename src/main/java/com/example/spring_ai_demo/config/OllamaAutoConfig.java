package com.example.spring_ai_demo.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;

@Configuration
public class OllamaAutoConfig {
    private static final Logger log = LoggerFactory.getLogger(OllamaAutoConfig.class);

    @Bean
    public OllamaApi ollamaApi(){
        return new OllamaApi("http://localhost:11434");
    }
    @Bean
    public OllamaChatModel chatModel(OllamaApi ollamaApi) {
        OllamaChatModel chatModel = new OllamaChatModel(ollamaApi,
                OllamaOptions.create()
                        .withModel("wangshenzhi/llama3-8b-chinese-chat-ollama-q4")
                        .withTemperature(0.9f));
        return chatModel;
    }
    @Bean
    public OllamaEmbeddingModel embeddingModel(OllamaApi ollamaApi){
        return new OllamaEmbeddingModel(ollamaApi).withDefaultOptions(OllamaOptions.create().withModel("nomic-embed-text"));
    }
    @Bean
    public VectorStore vectorStore(OllamaEmbeddingModel embeddingModel) {
        log.info("开始加载本地知识库文档");
        SimpleVectorStore vectorStore = new SimpleVectorStore(embeddingModel);
        File files = new File(ClassLoader.getSystemResource("doc").getFile());

        for(File f:files.listFiles()){
            Resource resource = new FileSystemResource(f);
            TextReader textReader = new TextReader(resource);
            textReader.setCharset(Charset.defaultCharset());
            vectorStore.add(textReader.get());
            log.info("知识库文件加载完成：{}",f.getName());
        }
        return vectorStore;
    }
}
