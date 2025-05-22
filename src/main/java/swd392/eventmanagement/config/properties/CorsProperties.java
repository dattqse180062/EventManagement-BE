package swd392.eventmanagement.config.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class CorsProperties {
    private Frontend frontend = new Frontend();
    private Backend backend = new Backend();

    public List<String> getAllowedOrigins() {
        List<String> origins = new ArrayList<>();

        String frontendUrl = frontend.getUrl();
        if (frontendUrl != null && !frontendUrl.isEmpty()) {
            origins.add(frontendUrl);
        }

        String backendUrl = backend.getUrl();
        if (backendUrl != null && !backendUrl.isEmpty()) {
            origins.add(backendUrl);
        }

        return origins;
    }

    @Getter
    @Setter
    public static class Frontend {
        private String host;
        private String port;
        private String url;

        public String getUrl() {
            if (url != null && !url.isEmpty()) {
                return url;
            }
            return "http://" + host + ":" + port;
        }
    }

    @Getter
    @Setter
    public static class Backend {
        private String url;
    }
}
