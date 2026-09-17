package model;

import lombok.Data;

import java.util.Date;

/**
 * 客服会话实体类
 */
@Data
public class ChatSession {
    private Integer id;           // 会话ID
    private Integer uid;          // 用户ID
    private String status;        // 会话状态 OPEN=进行中 CLOSED=已结束
    private Date createdTime;     // 创建时间
    private Date updatedTime;     // 更新时间
}
