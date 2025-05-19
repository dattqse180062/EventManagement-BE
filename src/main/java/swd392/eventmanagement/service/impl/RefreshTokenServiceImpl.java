package swd392.eventmanagement.service.impl;

import org.springframework.stereotype.Service;
import swd392.eventmanagement.config.properties.JwtProperties;
import swd392.eventmanagement.exception.TokenRefreshException;
import swd392.eventmanagement.model.entity.RefreshToken;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.RefreshTokenRepository;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.service.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenServiceImpl(
            JwtProperties jwtProperties,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository) {
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshExpiration() / 1000));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setIssuedAt(LocalDateTime.now());

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }
    
    @Override
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> refreshTokenRepository.delete(refreshToken));
    }
}