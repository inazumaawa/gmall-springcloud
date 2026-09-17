package obs.config;

import com.obs.services.ObsClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * OBS 配置类 — 支持多桶（公共桶/私有桶），通过 Nacos 动态刷新
 * <p>
 * Nacos 配置示例：
 * <pre>
 * obs:
 *   endpoint: https://obs.cn-north-4.myhuaweicloud.com
 *   ak: your-access-key
 *   sk: your-secret-key
 *   buckets:
 *     public:
 *       name: mall-public
 *       type: PUBLIC
 *       base-url: https://mall-public.obs.cn-north-4.myhuaweicloud.com
 *     private:
 *       name: mall-private
 *       type: PRIVATE
 *       base-url: https://mall-private.obs.cn-north-4.myhuaweicloud.com
 * </pre>
 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "obs")
public class ObsConfig {
    private String endpoint;
    private String ak;
    private String sk;
    /** key=bucketKey, value=bucketConfig */
    private Map<String, BucketConfig> buckets;

    @Data
    public static class BucketConfig {
        private String name;
        /** PUBLIC / PRIVATE */
        private String type = "PUBLIC";
        private String baseUrl;
    }

    @Bean
    public ObsClient obsClient() {
        return new ObsClient(ak, sk, endpoint);
    }

    /**
     * 根据桶 key 获取桶名，默认 public
     */
    public String getBucketName(String bucketKey) {
        if (buckets == null || !buckets.containsKey(bucketKey)) {
            throw new IllegalArgumentException("未配置的桶: " + bucketKey);
        }
        return buckets.get(bucketKey).getName();
    }

    /**
     * 根据桶 key 获取访问基础 URL
     */
    public String getBaseUrl(String bucketKey) {
        if (buckets == null || !buckets.containsKey(bucketKey)) {
            throw new IllegalArgumentException("未配置的桶: " + bucketKey);
        }
        return buckets.get(bucketKey).getBaseUrl();
    }
}
