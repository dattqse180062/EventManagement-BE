package swd392.eventmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {


    @Value("${app.backend.url}")
    private String backendUrl;

    @Value("${server.servlet.context-path:}")
    private String contextPath;    @Bean
    public OpenAPI myOpenAPI() {
        // Create a hardcoded server URL to ensure it's properly formatted
        Server devServer = new Server();
        devServer.setUrl(backendUrl + contextPath);
        devServer.setDescription("Server URL in Development environment");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Event Management System API")
                .version("1.0")
                .description("This API exposes endpoints for managing events.")
                .license(mitLicense);        // Define the JWT security scheme with more details
        SecurityScheme bearerAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token with the 'Bearer ' prefix");

        // Create security requirement named "bearerAuth"
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerAuthScheme))
                .addSecurityItem(securityRequirement);
    }
}
