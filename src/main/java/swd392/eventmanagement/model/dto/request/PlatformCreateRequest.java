package swd392.eventmanagement.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlatformCreateRequest {
    @NotBlank(message = "Platform name cannot be empty when platform is provided")
    @Size(max = 100, message = "Platform name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Platform URL cannot be empty when platform is provided")
    @Size(max = 255, message = "URL cannot exceed 255 characters")
    private String url;
}
