package ws.service;

import model.ChatMessage;
import model.ChatSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ws.mapper.ChatMapper;

import java.util.Date;
import java.util.List;

/**
 * 客服业务逻辑
 */
@Service
public class ChatService {

    @Autowired
    private ChatMapper chatMapper;

    /** 获取用户进行中的会话，没有则新建 */
    public int ensureSession(Integer uid) {
        ChatSession open = chatMapper.findOpenSession(uid);
        if (open != null) {
            return open.getId();
        }
        ChatSession session = new ChatSession();
        session.setUid(uid);
        session.setStatus("OPEN");
        session.setCreatedTime(new Date());
        session.setUpdatedTime(new Date());
        chatMapper.insertSession(session);
        return session.getId();
    }

    public void saveMessage(ChatMessage message) {
        chatMapper.insertMessage(message);
    }

    public List<ChatMessage> history(Integer sessionId) {
        return chatMapper.findMessages(sessionId);
    }

    public List<ChatSession> listSessions() {
        return chatMapper.listSessions();
    }

    public void closeSession(Integer sessionId) {
        chatMapper.closeSession(sessionId);
    }
}
