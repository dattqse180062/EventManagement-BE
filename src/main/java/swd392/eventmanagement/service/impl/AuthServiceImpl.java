package swd392.eventmanagement.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.config.properties.DomainAuthProperties;
import swd392.eventmanagement.exception.TokenRefreshException;
import swd392.eventmanagement.model.dto.request.GoogleTokenRequest;
import swd392.eventmanagement.model.dto.response.JwtResponse;
import swd392.eventmanagement.model.dto.response.TokenRefreshResponse;
import swd392.eventmanagement.model.entity.RefreshToken;
import swd392.eventmanagement.model.entity.Role;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.RoleRepository;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.security.jwt.JwtUtils;
import swd392.eventmanagement.service.AuthService;
import swd392.eventmanagement.service.GoogleTokenVerifierService;
import swd392.eventmanagement.service.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final DomainAuthProperties domainAuthProperties;
    
    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            JwtUtils jwtUtils,
            RefreshTokenService refreshTokenService,
            GoogleTokenVerifierService googleTokenVerifierService,
            DomainAuthProperties domainAuthProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.domainAuthProperties = domainAuthProperties;
    }
    
    @Override
    public JwtResponse authenticateWithGoogle(GoogleTokenRequest request) throws Exception {
        Payload payload = googleTokenVerifierService.verifyGoogleIdToken(request.getIdToken());
        
        if (payload == null) {
            logger.warn("Invalid Google ID token provided");
            throw new Exception("Invalid ID token");
        }
        
        String email = payload.getEmail();
        
        if (!googleTokenVerifierService.isAllowedDomain(email)) {
            logger.warn("Access attempt with unauthorized email domain: {}", email);
            throw new Exception("Access denied: Only allowed email domains can access this service");
        }
        
        String name = (String) payload.get("name");
        String providerId = payload.getSubject();
        
        User user = createOrUpdateUser(email, name, providerId);
        
        // Generate JWT and refresh token
        String token = jwtUtils.generateTokenFromEmail(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        
        // Extract role names from user roles
        Set<String> roles = new HashSet<>(user.getRoles()).stream()
            .map(role -> role.getName().replace("ROLE_", ""))
            .collect(Collectors.toSet());
        
        logger.info("User successfully authenticated: {}", email);
        return new JwtResponse(
            token, 
            refreshToken.getToken(), 
            user.getId(), 
            user.getEmail(), 
            user.getFullName(),
            roles);
    }
    
    @Override
    public TokenRefreshResponse refreshToken(String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    User user = token.getUser();
                    String jwt = jwtUtils.generateTokenFromEmail(user.getEmail());
                    logger.info("Token refreshed for user: {}", user.getEmail());
                    return new TokenRefreshResponse(jwt, refreshToken);
                })
                .orElseThrow(() -> {
                    logger.warn("Refresh token not found in database: {}", refreshToken);
                    return new TokenRefreshException(refreshToken, "Refresh token is not in database!");
                });
    }
    
    @Override
    public boolean logout(String refreshToken) {
        Optional<RefreshToken> tokenOptional = refreshTokenService.findByToken(refreshToken);
        if (tokenOptional.isPresent()) {
            refreshTokenService.deleteByToken(refreshToken);
            logger.info("User logged out successfully");
            return true;
        }
        logger.warn("Logout failed: refresh token not found or already invalidated.");
        return false;
    }
    
    @Override
    public User createOrUpdateUser(String email, String name, String providerId) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        
        if (userOptional.isEmpty()) {
            logger.info("Creating new user with email: {}", email);
            // Create new user
            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setProviderUserId(providerId);
              // Assign role based on email domain using the injected properties
            String roleName;
            String studentDomain = domainAuthProperties.getStudentDomain();
            String lecturerDomain = domainAuthProperties.getLecturerDomain();
            
            if (studentDomain != null && email.endsWith("@" + studentDomain)) {
                roleName = "ROLE_STUDENT";
            } else if (lecturerDomain != null && email.endsWith("@" + lecturerDomain)) {
                roleName = "ROLE_LECTURER";
            } else {
                roleName = "ROLE_STUDENT"; // Default role
            }
            
            Role userRole = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " is not found."));
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);
            
            userRepository.save(user);
        } else {
            user = userOptional.get();
            logger.info("Updating existing user: {}", email);
            // Update existing user info
            user.setFullName(name);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
        
        return user;
    }
}