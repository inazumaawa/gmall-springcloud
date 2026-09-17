package customerService.config;

import customerService.store.RedisChatMemoryStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AiConfig {

    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    // 会话记忆提供者：基于 RedisChatMemoryStore 持久化，最大保留 20 条消息
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return new ChatMemoryProvider() {
            @Override 
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .maxMessages(20)
                        .id(memoryId)
                        .chatMemoryStore(redisChatMemoryStore)
                        .build();
            }
        };
    }

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String openAiBaseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String openAiApiKey;

    // 构建 EmbeddingModel，用于文本向量化（使用阿里云 DashScope OpenAI 兼容接口）
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(openAiApiKey)
                .modelName("text-embedding-v3")
                .build();
    }

    //    构建向量数据库操作对象
//    负责数据的写入和存储，即将文档切分、向量化并存入向量数据库。
    @Bean
    public EmbeddingStore embeddingStore(EmbeddingModel embeddingModel) {
        InMemoryEmbeddingStore store = new InMemoryEmbeddingStore();
        List<Document> documents;
        try {
            System.out.println("=== 开始加载 RAG 文档 ===");
            documents = ClassPathDocumentLoader.loadDocuments("content", new ApacheTikaDocumentParser());
            System.out.println("=== 成功加载 " + documents.size() + " 个文档 ===");
        } catch (IllegalArgumentException e) {
            // RAG 文档目录不存在时跳过，不影响服务启动
            System.out.println("=== RAG 文档目录 'content' 不存在，跳过向量化 ===");
            return store;
        }

//        构建文档分割器
        DocumentSplitter recursive = DocumentSplitters.recursive(400, 150);

//        构建EmbeddingStoreIngestor对象完成文本数据切割，向量化，存储
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(recursive)
                .build();

        // DashScope API 限制每次最多10条，分批 ingest
        int batchSize = 10;
        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            List<Document> batch = documents.subList(i, end);
            System.out.println("=== 正在 ingest 第 " + (i + 1) + "~" + end + " 个文档 ===");
            ingestor.ingest(batch);
        }
        return store;
    }

    //    构建向量数据库检索对象
    //    负责数据的查询和检索，即根据输入查询条件从向量数据库中查找最相关的数据。
    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore store, EmbeddingModel embeddingModel) {
//        构建向量数据库检索对象（必须使用同一个 EmbedwwzwzadingModel，否则向量维度不匹配）
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .minScore(0.8)
                .maxResults(2)
                .build();
    }

    /**
     * 图像识别专用 ChatModel（同步版，保留以备非流式场景使用）
     */
    @Bean(name = "imageChatModel")
    public OpenAiChatModel imageChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(openAiApiKey)
                .modelName("qwen-vl-max")
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 图像识别流式 ChatModel：使用 qwen-vl-max 多模态模型，支持 SSE 逐 token 输出
     */
    @Bean(name = "imageStreamingChatModel")
    public OpenAiStreamingChatModel imageStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(openAiApiKey)
                .modelName("qwen-vl-max")
                .logRequests(true)
                .logResponses(true)
                .build();
    }

}
