package ws.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ws.service.ChatService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 客服聊天 WebSocket 处理器
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    /** uid -> 用户连接 */
    private static final Map<Integer, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();
    /** 客服端连接列表 */
    private static final List<WebSocketSession> ADMIN_SESSIONS = new CopyOnWriteArrayList<>();

    @Autowired
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String role = String.valueOf(session.getAttributes().get("role"));
        if ("admin".equals(role)) {
            ADMIN_SESSIONS.add(session);
        } else {
            Integer uid = (Integer) session.getAttributes().get("uid");
            if (uid != null) {
                USER_SESSIONS.put(uid, session);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String content = payload.get("content") == null ? "" : String.valueOf(payload.get("content"));
        if (content.trim().isEmpty()) {
            return;
        }

        String role = String.valueOf(session.getAttributes().get("role"));
        Integer uid = (Integer) session.getAttributes().get("uid");

        ChatMessage msg = new ChatMessage();
        msg.setContent(content);
        msg.setSendTime(new Date());

        if ("admin".equals(role)) {
            // 客服回复指定用户
            Integer toUid = toInt(payload.get("toUid"));
            Integer sessionId = toInt(payload.get("sessionId"));
            msg.setSenderId(uid == null ? 0 : uid);
            msg.setSenderRole("admin");
            msg.setSessionId(sessionId);
            chatService.saveMessage(msg);
            pushToUser(toUid, msg);
        } else {
            // 用户发送消息给客服
            int sessionId = chatService.ensureSession(uid);
            msg.setSenderId(uid);
            msg.setSenderRole("user");
            msg.setSessionId(sessionId);
            chatService.saveMessage(msg);
            pushToAdmins(msg);
        }
    }

    private Integer toInt(Object o) {
        return o == null ? null : Integer.valueOf(o.toString());
    }

    private void pushToAdmins(ChatMessage msg) throws Exception {
        String json = objectMapper.writeValueAsString(msg);
        for (WebSocketSession s : ADMIN_SESSIONS) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }

    private void pushToUser(Integer uid, ChatMessage msg) throws Exception {
        if (uid == null) {
            return;
        }
        WebSocketSession s = USER_SESSIONS.get(uid);
        if (s != null && s.isOpen()) {
            s.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String role = String.valueOf(session.getAttributes().get("role"));
        if ("admin".equals(role)) {
            ADMIN_SESSIONS.remove(session);
        } else {
            Integer uid = (Integer) session.getAttributes().get("uid");
            if (uid != null) {
                USER_SESSIONS.remove(uid, session);
            }
        }
    }
}
