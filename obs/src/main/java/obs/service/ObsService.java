package obs.service;

import java.io.InputStream;
import java.util.List;

/**
 * OBS 存储服务接口
 */
public interface ObsService {

    /**
     * 上传文件到指定桶
     *
     * @param bucketKey  桶标识（如 "public"/"private"）
     * @param objectKey  对象路径（如 "goods/2024/abc.jpg"）
     * @param inputStream 文件流
     * @param contentType MIME 类型
     * @return 可访问的 URL
     */
    String upload(String bucketKey, String objectKey, InputStream inputStream, String contentType);

    /**
     * 获取文件访问 URL
     */
    String getUrl(String bucketKey, String objectKey);

    /**
     * 删除文件
     */
    void delete(String bucketKey, String objectKey);

    /**
     * 替换文件（先删后传）
     *
     * @return 新的访问 URL
     */
    String replace(String bucketKey, String objectKey, InputStream inputStream, String contentType);

    /**
     * 列出指定桶下指定前缀的对象
     */
    List<String> list(String bucketKey, String prefix);
}
