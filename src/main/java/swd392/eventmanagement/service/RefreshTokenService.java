package swd392.eventmanagement.service;

import swd392.eventmanagement.model.entity.RefreshToken;
import java.util.Optional;

public interface RefreshTokenService {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken createRefreshToken(Long userId);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByToken(String token);
} 