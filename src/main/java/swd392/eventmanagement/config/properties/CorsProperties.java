package swd392.eventmanagement.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.frontend")
@Getter
@Setter
public class CorsProperties {
    private String host;
    private String port;
    private String frontendUrl;
    
    /**
     * Get the frontend URL, constructing it from host and port if it's not directly set
     */
    public String getFrontendUrl() {
        if (frontendUrl != null && !frontendUrl.isEmpty()) {
            return frontendUrl;
        }
        return "http://" + host + ":" + port;
    }
}
