package ws.mapper;

import model.ChatMessage;
import model.ChatSession;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 客服会话与消息数据访问层
 */
@Mapper
public interface ChatMapper {

    @Select("SELECT id, uid, status, created_time AS createdTime, updated_time AS updatedTime " +
            "FROM chat_session WHERE uid = #{uid} AND status = 'OPEN' ORDER BY id DESC LIMIT 1")
    ChatSession findOpenSession(Integer uid);

    @Insert("INSERT INTO chat_session(uid, status, created_time, updated_time) " +
            "VALUES(#{uid}, #{status}, #{createdTime}, #{updatedTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertSession(ChatSession session);

    @Insert("INSERT INTO chat_message(session_id, sender_id, sender_role, content, send_time) " +
            "VALUES(#{sessionId}, #{senderId}, #{senderRole}, #{content}, #{sendTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertMessage(ChatMessage message);

    @Select("SELECT id, session_id AS sessionId, sender_id AS senderId, sender_role AS senderRole, " +
            "content, send_time AS sendTime FROM chat_message WHERE session_id = #{sessionId} ORDER BY id ASC")
    List<ChatMessage> findMessages(Integer sessionId);

    @Select("SELECT id, uid, status, created_time AS createdTime, updated_time AS updatedTime " +
            "FROM chat_session ORDER BY id DESC")
    List<ChatSession> listSessions();

    @Update("UPDATE chat_session SET status = 'CLOSED', updated_time = NOW() WHERE id = #{sessionId}")
    void closeSession(Integer sessionId);
}
