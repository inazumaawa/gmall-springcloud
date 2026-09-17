package customerService.service;

import customerService.store.RedisChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * 图像识别服务：使用 qwen-vl-max 多模态模型进行图像识别，结果持久化到 Redis 以支持多轮对话
 */
@Slf4j
@Service
public class ImageRecognitionService {

    private final OpenAiStreamingChatModel imageStreamingChatModel;
    private final RedisChatMemoryStore chatMemoryStore;

    /** 会话记忆最大保留消息条数 */
    private static final int MAX_MESSAGES = 20;

    public ImageRecognitionService(@Qualifier("imageStreamingChatModel") OpenAiStreamingChatModel imageStreamingChatModel,
                                   RedisChatMemoryStore chatMemoryStore) {
        this.imageStreamingChatModel = imageStreamingChatModel;
        this.chatMemoryStore = chatMemoryStore;
    }

    /**
     * 图像识别流式对话，支持同时传入图片和文本，对话记录持久化到 Redis
     *
     * @param memoryId     会话 ID（用于多轮对话记忆）
     * @param userText     用户文本输入（可为 null 或空）
     * @param imageDataUrl 图片的 Base64 Data URL（格式：data:image/xxx;base64,...）
     * @return SSE 流式响应 Flux
     */
    public Flux<String> streamImageChat(String memoryId, String userText, String imageDataUrl) {
        // 解析 data URL 提取 mimeType 和纯 base64 数据
        int colonIdx = imageDataUrl.indexOf(':');
        int semicolonIdx = imageDataUrl.indexOf(';');
        int commaIdx = imageDataUrl.indexOf(',');
        String mimeType = imageDataUrl.substring(colonIdx + 1, semicolonIdx);
        String base64Data = imageDataUrl.substring(commaIdx + 1);

        String text = (userText != null && !userText.isBlank()) ? userText : "请描述这张图片";

        UserMessage userMessage = UserMessage.from(
                TextContent.from(text),
                ImageContent.from(base64Data, mimeType)
        );

        return doChatAndPersist(memoryId, userMessage);
    }

    /**
     * 通过公开 URL 进行图像识别（支持 http/https 图片链接）
     *
     * @param memoryId 会话 ID
     * @param userText 用户文本输入（可为 null 或空）
     * @param imageUrl 图片的公开 URL
     * @return SSE 流式响应 Flux
     */
    public Flux<String> streamImageChatByUrl(String memoryId, String userText, String imageUrl) {
        String text = (userText != null && !userText.isBlank()) ? userText : "请描述这张图片";

        UserMessage userMessage = UserMessage.from(
                TextContent.from(text),
                ImageContent.from(imageUrl)
        );

        return doChatAndPersist(memoryId, userMessage);
    }

    /**
     * 流式调用模型并持久化对话记录：逐 token 推送到 SSE，完成后将完整对话写入 Redis
     */
    private Flux<String> doChatAndPersist(String memoryId, UserMessage userMessage) {
        return Flux.create(sink -> {
            StringBuilder fullResult = new StringBuilder();

            // 流式回调处理器：模型每生成一个token触发onPartialResponse，完成后触发onCompleteResponse
            imageStreamingChatModel.chat(List.of(userMessage), new StreamingChatResponseHandler() {
                @Override
                // 模型每生成一个 token 就回调一次，将 token 拼入完整结果并推送到 SSE
                public void onPartialResponse(String token) {
                    fullResult.append(token);
                    sink.next(token);
                }

                @Override
                // 模型流式输出完毕后回调，将完整 AI 回答持久化到 Redis 并关闭 SSE 流
                public void onCompleteResponse(ChatResponse response) {
                    String result = response.aiMessage().text();
                    log.info("图片识别结果: {}", result);
                    persistMessages(memoryId, userMessage, result);
                    sink.complete();
                }

                @Override
                // 流式输出发生异常时回调，终止 SSE 流并传递错误
                public void onError(Throwable error) {
                    log.error("图片识别流式输出失败: {}", error.getMessage());
                    sink.error(error);
                }
            });
        });
    }

    /**
     * 持久化对话消息到 Redis（图片对话仅记录文本内容，避免 ImageContent 序列化问题）
     */
    private void persistMessages(String memoryId, UserMessage userMessage, String aiText) {
        try {
            List<ChatMessage> history = new ArrayList<>(chatMemoryStore.getMessages(memoryId));
            if (history.size() > MAX_MESSAGES) {
                history = new ArrayList<>(history.subList(history.size() - MAX_MESSAGES, history.size()));
            }
            // 提取文本内容保存（图片 base64 不序列化，避免膨胀 Redis 和序列化兼容问题）
            String textPart = "[用户发送了一张图片]";
            for (var content : userMessage.contents()) {
                if (content instanceof TextContent) {
                    textPart = ((TextContent) content).text();
                    break;
                }
            }
            history.add(UserMessage.from(textPart));
            history.add(AiMessage.from(aiText));
            chatMemoryStore.updateMessages(memoryId, history);
        } catch (Exception e) {
            log.warn("持久化图片对话记录失败: {}", e.getMessage());
        }
    }
}


