package model;

import lombok.Data;

import java.util.Date;

/**
 * 客服消息实体类
 */
@Data
public class ChatMessage {
    private Integer id;           // 消息ID
    private Integer sessionId;    // 会话ID
    private Integer senderId;     // 发送人ID
    private String senderRole;    // 发送人角色 user=用户 admin=客服
    private String content;       // 消息内容
    private Date sendTime;        // 发送时间
    private Integer toUid;        // 仅用于 WebSocket 路由（客服回复目标用户），不落库
}
