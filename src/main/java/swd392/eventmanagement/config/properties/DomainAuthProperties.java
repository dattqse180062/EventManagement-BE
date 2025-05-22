package swd392.eventmanagement.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.auth")
@Getter
@Setter
@ToString
public class DomainAuthProperties {
    private String allowedDomains;
    private String studentDomain;
    private String lecturerDomain;

    /**
     * Get allowed domains as a list
     */
    public List<String> getAllowedDomainsList() {
        if (allowedDomains == null || allowedDomains.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(allowedDomains.split(","));
    }
}
