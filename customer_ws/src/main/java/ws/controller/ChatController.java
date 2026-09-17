package ws.controller;

import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ws.service.ChatService;

/**
 * 客服 REST 接口
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /** 用户创建/获取会话 */
    @PostMapping("/session/create")
    public Result createSession(@RequestHeader("uid") Integer uid) {
        return Result.success(chatService.ensureSession(uid));
    }

    /** 查询会话历史消息 */
    @GetMapping("/history")
    public Result history(@RequestParam Integer sessionId) {
        return Result.success(chatService.history(sessionId));
    }

    /** 客服查询全部会话 */
    @GetMapping("/session/list")
    public Result listSessions() {
        return Result.success(chatService.listSessions());
    }

    /** 关闭会话 */
    @PostMapping("/session/close")
    public Result close(@RequestParam Integer sessionId) {
        chatService.closeSession(sessionId);
        return Result.success();
    }
}
