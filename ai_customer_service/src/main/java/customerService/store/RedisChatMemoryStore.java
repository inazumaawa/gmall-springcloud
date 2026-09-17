package customerService.store;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的 ChatMemoryStore 实现，使用 LangChain4j 内置的 ChatMessageSerializer/Deserializer 进行序列化
 */
@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    private static final String KEY_PREFIX = "ai:memory:";
    // 会话记忆保留 30 分钟
    private static final long TTL_MINUTES = 30;

    private final StringRedisTemplate redisTemplate;

    public RedisChatMemoryStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + memoryId);
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            throw new RuntimeException("反序列化聊天记录失败", e);
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        try {
            String json = ChatMessageSerializer.messagesToJson(messages);
            redisTemplate.opsForValue().set(KEY_PREFIX + memoryId, json, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("序列化聊天记录失败", e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete(KEY_PREFIX + memoryId);
    }
}
