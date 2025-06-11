package swd392.eventmanagement.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.exception.UserProcessingException;
import swd392.eventmanagement.model.dto.response.UserDTO;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.model.mapper.UserMapper;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.service.UserService;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDTO getCurrentUser() {
        logger.info("Getting current user information");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                logger.error("User not authenticated");
                throw new UserNotFoundException("User not authenticated");
            }

            String email = authentication.getName();
            logger.debug("Fetching user with email: {}", email);
            User user = getUserByEmail(email);

            logger.info("Successfully retrieved information for user: {}", email);
            return userMapper.toUserDTO(user);
        } catch (UserNotFoundException e) {
            // Just rethrow UserNotFoundException without additional processing
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving current user information", e);
            throw new UserProcessingException("Failed to retrieve user information", e);
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            logger.debug("Looking up user with email: {}", email);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        } catch (UserNotFoundException e) {
            logger.error("User not found with email: {}", email);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user with email: {}", email, e);
            throw new UserProcessingException("Failed to retrieve user by email", e);
        }
    }

    @Override
    public List<UserDTO> getUsersNotInDepartment() {
        logger.info("Getting users not assigned to any role in any department");
        try {
            List<User> users = userRepository.findLecturerUsersNotAssignedToAnyDepartmentRole();

            if (users.isEmpty()) {
                logger.info("No unassigned users found in any department");
                throw new UserNotFoundException("No users found without department roles");
            }

            logger.info("Found {} users not assigned to any department role", users.size());
            return userMapper.toUserShowDTOList(users);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving users not assigned to any department role", e);
            throw new UserProcessingException("Failed to retrieve users not assigned to department roles", e);
        }
    }
}