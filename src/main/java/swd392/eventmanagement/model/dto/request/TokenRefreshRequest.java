package swd392.eventmanagement.model.dto.request;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
} 