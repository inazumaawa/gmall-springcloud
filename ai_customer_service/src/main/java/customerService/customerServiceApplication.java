package customerService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class customerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(customerServiceApplication.class, args);
    }
}
