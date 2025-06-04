package swd392.eventmanagement.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleTokenRequest {
    @NotBlank(message = "ID token cannot be blank")
    private String idToken;
}