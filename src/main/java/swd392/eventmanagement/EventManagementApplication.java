package swd392.eventmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import swd392.eventmanagement.config.properties.JwtProperties;
import swd392.eventmanagement.config.properties.CorsProperties;
import swd392.eventmanagement.config.properties.DomainAuthProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        JwtProperties.class,
        CorsProperties.class,
        DomainAuthProperties.class
})
public class EventManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventManagementApplication.class, args);
    }
}