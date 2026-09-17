package ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 客服 WebSocket 实时通讯服务
 */
@SpringBootApplication
@EnableDiscoveryClient
public class CustomerWsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerWsApplication.class, args);
    }
}
