package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.eventmanagement.model.entity.RefreshToken;
import swd392.eventmanagement.model.entity.User;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
} 