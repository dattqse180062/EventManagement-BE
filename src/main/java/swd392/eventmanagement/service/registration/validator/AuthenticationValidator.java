package swd392.eventmanagement.service.registration.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import swd392.eventmanagement.exception.AccessDeniedException;
import swd392.eventmanagement.exception.EventRegistrationException;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.security.service.UserDetailsImpl;

@Component
@RequiredArgsConstructor
public class AuthenticationValidator {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationValidator.class);

    private final UserRepository userRepository;

    /**
     * Authenticates user and validates email match
     */
    public User authenticateAndGetUser(String email) {
        logger.info("Authenticating user with email: {}", email);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.error("User authentication failed - no valid authentication context");
            throw new AccessDeniedException("User not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        logger.debug("Found authenticated user with ID: {} and email: {}", userDetails.getId(), userDetails.getEmail());

        if (!userDetails.getEmail().equals(email)) {
            logger.error("Email mismatch - authenticated user email: {}, requested email: {}",
                    userDetails.getEmail(), email);
            throw new EventRegistrationException("User email does not match authenticated user");
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> {
                    logger.error("User not found in database with ID: {}", userDetails.getId());
                    return new UserNotFoundException("User not found");
                });

        logger.info("Successfully authenticated user: {} (ID: {})", user.getEmail(), user.getId());
        return user;
    }

    /**
     * Gets the current authenticated user
     */
    public User getCurrentAuthenticatedUser() {
        logger.info("Getting current authenticated user");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.error("No authenticated user found in security context");
            throw new AccessDeniedException("User not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        logger.debug("Found authenticated user with ID: {}", userDetails.getId());

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> {
                    logger.error("Authenticated user not found in database with ID: {}", userDetails.getId());
                    return new UserNotFoundException("User not found");
                });

        logger.info("Successfully retrieved current user: {} (ID: {})", user.getEmail(), user.getId());
        return user;
    }

    /**
     * Gets user by email
     */
    public User getUserByEmail(String email) {
        logger.info("Looking up user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });

        logger.info("Successfully found user: {} (ID: {})", user.getEmail(), user.getId());
        return user;
    }
}
