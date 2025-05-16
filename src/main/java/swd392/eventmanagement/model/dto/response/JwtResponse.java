package swd392.eventmanagement.model.dto.response;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String fullName;
    
    public JwtResponse(String token, String refreshToken, Long id, String email, String fullName) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
    }
} 