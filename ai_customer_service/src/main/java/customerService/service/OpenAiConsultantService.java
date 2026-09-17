package customerService.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel", 
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever",
        tools = {"cartTool"}
    )


public interface OpenAiConsultantService {

     @SystemMessage(fromResource = "system.txt")
    public Flux<String> streamChat(@MemoryId String memoryId, @UserMessage String message);
}
