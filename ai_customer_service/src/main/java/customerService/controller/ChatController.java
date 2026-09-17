package customerService.controller;

import customerService.service.ImageRecognitionService;
import customerService.service.OpenAiConsultantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.Base64;

@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    private OpenAiConsultantService consultantService;

    @Autowired
    private ImageRecognitionService imageRecognitionService;

    /**
     * 纯文本流式对话
     */
    @RequestMapping(value = "/chat", produces = "text/event-stream;charset=utf-8")
    public Flux<String> openAiStream(@RequestParam("message") String message, @RequestHeader("uid") String memoryId) {
        return consultantService.streamChat(memoryId, message);
    }

    /**
     * 图片识别流式对话：上传图片并可选附带文字描述，使用 Qwen3.6-Plus 进行多模态识别
     */
    @PostMapping(value = "/chat-image", produces = "text/event-stream;charset=utf-8")
    public Flux<String> chatWithImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "message", required = false) String message,
            @RequestHeader("uid") String memoryId) {
        try {
            // 将图片转为 Base64 Data URL
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
            String contentType = image.getContentType() != null ? image.getContentType() : "image/jpeg";
            String dataUrl = "data:" + contentType + ";base64," + base64;

            return imageRecognitionService.streamImageChat(memoryId, message, dataUrl);
        } catch (Exception e) {
            return Flux.error(new RuntimeException("图片处理失败: " + e.getMessage(), e));
        }
    }

    /**
     * 图片识别流式对话（通过 URL）：传入公开图片链接进行多模态识别
     */
    @PostMapping(value = "/chat-image-url", produces = "text/event-stream;charset=utf-8")
    public Flux<String> chatWithImageUrl(
            @RequestParam("url") String imageUrl,
            @RequestParam(value = "message", required = false) String message,
            @RequestHeader("uid") String memoryId) {
        return imageRecognitionService.streamImageChatByUrl(memoryId, message, imageUrl);
    }

}
