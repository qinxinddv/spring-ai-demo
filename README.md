# 基于spring-ai框架实现的AI对话demo
基于RAG本地知识库检索效果如图：
![spring-ai-demo](https://github.com/qinxinddv/spring-ai-demo/assets/10646396/64787c10-e568-4ad9-bf08-52f7d306b3e9)
知识库内容：  
[resources/doc/pet.txt](https://github.com/qinxinddv/spring-ai-demo/blob/main/src/main/resources/doc/pet.txt)  
[resources/doc/pet-rule.txt](https://github.com/qinxinddv/spring-ai-demo/blob/main/src/main/resources/doc/pet-rule.txt)  
# 接口AIController
## ai对话接口
- 地址：/ai/generate
- 请求方式：GET
- 参数：message
- 响应：ai回答内容文本
## ai对话接口-流式响应
- 地址：/ai/generateStream
- 请求方式：GET
- 参数：message
- 响应：ai回答内容文本

## ai对话接口-基于本地知识库的RAG增强检索
- 地址：/ai/query
- 请求方式：GET
- 参数：message
- 响应：ai回答内容文本
- 知识库文件存放路径：resources/doc
