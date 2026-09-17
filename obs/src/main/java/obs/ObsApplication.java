package obs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ObsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ObsApplication.class, args);
    }
}
