package swd392.eventmanagement.service;

import swd392.eventmanagement.model.dto.request.GoogleTokenRequest;
import swd392.eventmanagement.model.dto.response.JwtResponse;
import swd392.eventmanagement.model.dto.response.TokenRefreshResponse;
import swd392.eventmanagement.model.entity.User;

public interface AuthService {
    JwtResponse authenticateWithGoogle(GoogleTokenRequest request) throws Exception;
    TokenRefreshResponse refreshToken(String refreshToken);
    boolean logout(String refreshToken);
    User createOrUpdateUser(String email, String name, String providerId);
} 