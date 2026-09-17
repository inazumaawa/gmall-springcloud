package obs.controller;

import obs.service.ObsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/obs")
public class ObsController {

    @Autowired
    private ObsService obsService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bucketKey", defaultValue = "public") String bucketKey,
            @RequestParam("objectKey") String objectKey) throws IOException {
        String url = obsService.upload(bucketKey, objectKey, file.getInputStream(), file.getContentType());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("data", Map.of("url", url));
        return result;
    }

    /**
     * 获取文件 URL
     */
    @GetMapping("/url")
    public Map<String, Object> getUrl(
            @RequestParam(value = "bucketKey", defaultValue = "public") String bucketKey,
            @RequestParam("objectKey") String objectKey) {
        String url = obsService.getUrl(bucketKey, objectKey);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("data", Map.of("url", url));
        return result;
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public Map<String, Object> delete(
            @RequestParam(value = "bucketKey", defaultValue = "public") String bucketKey,
            @RequestParam("objectKey") String objectKey) {
        obsService.delete(bucketKey, objectKey);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "删除成功");
        return result;
    }

    /**
     * 替换文件（覆盖上传）
     */
    @PutMapping("/replace")
    public Map<String, Object> replace(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bucketKey", defaultValue = "public") String bucketKey,
            @RequestParam("objectKey") String objectKey) throws IOException {
        String url = obsService.replace(bucketKey, objectKey, file.getInputStream(), file.getContentType());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("data", Map.of("url", url));
        return result;
    }

    /**
     * 列出文件
     */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(value = "bucketKey", defaultValue = "public") String bucketKey,
            @RequestParam(value = "prefix", defaultValue = "") String prefix) {
        List<String> urls = obsService.list(bucketKey, prefix);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("data", Map.of("files", urls));
        return result;
    }
}
