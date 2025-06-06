package swd392.eventmanagement.service.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.enums.RegistrationStatus;
import swd392.eventmanagement.enums.TargetAudience;
import swd392.eventmanagement.exception.EventRegistrationException;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.exception.AccessDeniedException;
import swd392.eventmanagement.exception.EventNotFoundException;
import swd392.eventmanagement.exception.EventRegistrationConflictException;
import swd392.eventmanagement.exception.StaffRoleNotFoundException;
import swd392.eventmanagement.model.dto.request.RegistrationCreateRequest;
import swd392.eventmanagement.model.dto.response.CheckinResponse;
import swd392.eventmanagement.model.dto.response.RegistrationCancelResponse;
import swd392.eventmanagement.model.dto.response.RegistrationCreateResponse;
import swd392.eventmanagement.model.dto.response.RegistrationSearchResponse;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventCapacity;
import swd392.eventmanagement.model.entity.Registration;
import swd392.eventmanagement.model.entity.Role;
import swd392.eventmanagement.model.entity.StaffRole;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.EventCapacityRepository;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.RegistrationRepository;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.repository.StaffRoleRepository;
import swd392.eventmanagement.security.service.UserDetailsImpl;
import swd392.eventmanagement.service.RegistrationService;
import swd392.eventmanagement.model.mapper.RegistrationMapper;
import swd392.eventmanagement.repository.EventStaffRepository;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final EventCapacityRepository eventCapacityRepository;
    private final EventStaffRepository eventStaffRepository;
    private final StaffRoleRepository staffRoleRepository;
    private final RegistrationMapper registrationMapper;

    @Override
    @Transactional
    public RegistrationCreateResponse createRegistration(RegistrationCreateRequest request) {
        logger.info("Creating new registration for event ID: {}", request.getEventId());

        User user = authenticateAndGetUser(request.getEmail());

        // Validate event
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventRegistrationException("Event is not open for registration");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getRegistrationStart()) || now.isAfter(event.getRegistrationEnd())) {
            throw new EventRegistrationException("Registration must be done within registration period");
        }

        validateUserEligibility(user, event);
        validateRegistrationCapacity(user.getRoles(), event);

        Registration registration = createAndSaveRegistration(user, event, request.getCheckinUrl());
        RegistrationCreateResponse response = registrationMapper.toRegistrationCreateResponse(registration);

        logger.info("Successfully created registration ID: {} for event: {} and user: {}",
                registration.getId(), event.getId(), user.getId());

        return response;
    }

    @Override
    @Transactional
    public RegistrationCancelResponse cancelRegistration(Long eventId) {
        logger.info("Canceling registration for event ID: {}", eventId);

        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("User not authenticated");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validate event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventRegistrationException("Event is not open for registration");
        }

        // Find registration
        Registration registration = registrationRepository.findByUserIdAndEventId(user.getId(), eventId)
                .orElseThrow(() -> new EventRegistrationException("Registration not found"));

        // Check if cancellation is within registration period
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getRegistrationStart()) || now.isAfter(event.getRegistrationEnd())) {
            throw new EventRegistrationException("Cancellation must be done within registration period");
        }

        // Check if cancellation is at least 1 day before event starts
        if (now.plusDays(1).isAfter(event.getStartTime())) {
            throw new EventRegistrationException("Cancellation must be done at least 1 day before event starts");
        }

        // Cancel registration
        registration.setStatus(RegistrationStatus.CANCELED);
        registration.setCanceledAt(now); // Set cancellation timestamp
        registration = registrationRepository.save(registration);

        RegistrationCancelResponse response = registrationMapper.toRegistrationCancelResponse(registration);

        logger.info("Successfully cancelled registration ID: {} for event: {} and user: {}",
                registration.getId(), event.getId(), user.getId());

        return response;
    }

    @Override
    public RegistrationSearchResponse searchRegistration(Long eventId, String email) {
        logger.info("Searching registration for event ID: {} and email: {}", eventId, email);

        try {
            // Input validation
            if (eventId == null || email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Event ID and email are required");
            }

            // Validate that the current user is authorized to check-in participants
            validateCheckinStaffRole(eventId);

            // Get user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

            // Validate event
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

            // Find registration
            Registration registration = registrationRepository.findByUserIdAndEventId(user.getId(), eventId)
                    .orElseThrow(() -> new EventRegistrationException("Email not registered for this event"));

            // Map to response using mapper
            RegistrationSearchResponse response = registrationMapper.toRegistrationSearchResponse(registration);

            logger.info("Successfully found registration - Registration ID: {}, User: {}, Event: {}",
                    registration.getId(), user.getEmail(), event.getId());

            return response;

        } catch (UserNotFoundException | EventNotFoundException | EventRegistrationException e) {
            logger.error("Registration search failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during registration search: {}", e.getMessage(), e);
            throw new EventRegistrationException("An unexpected error occurred during registration search");
        }
    }

    @Override
    @Transactional
    public CheckinResponse checkin(Long eventId, String email) {
        logger.info("Processing check-in for event ID: {} and email: {}", eventId, email);

        try {
            // Input validation
            if (eventId == null || email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Event ID and email are required");
            }

            // Validate that the current user is authorized to check-in participants
            validateCheckinStaffRole(eventId);

            // Get user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

            // Validate event
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

            // Find registration
            Registration registration = registrationRepository.findByUserIdAndEventId(user.getId(), eventId)
                    .orElseThrow(() -> new EventRegistrationException("Email not registered for this event"));

            // Check registration status
            validateRegistrationStatus(registration);

            // Validate check-in time
            validateCheckinTime(event);

            // Update registration status and checkin time
            LocalDateTime now = LocalDateTime.now();
            registration.setStatus(RegistrationStatus.ATTENDED);
            registration.setCheckinAt(now);
            registration = registrationRepository.save(registration);

            // Map to response using mapper
            CheckinResponse response = registrationMapper.toCheckinResponse(registration);

            logger.info("Successfully checked in - Registration ID: {}, User: {}, Event: {}",
                    registration.getId(), user.getEmail(), event.getId());

            return response;

        } catch (UserNotFoundException | EventNotFoundException | EventRegistrationException e) {
            logger.error("Check-in failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during check-in: {}", e.getMessage(), e);
            throw new EventRegistrationException("An unexpected error occurred during check-in");
        }
    }

    private User authenticateAndGetUser(String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("User not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        if (!userDetails.getEmail().equals(email)) {
            throw new EventRegistrationException("User email does not match authenticated user");
        }

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void validateUserEligibility(User user, Event event) {
        Set<Role> userRoles = user.getRoles();

        // Check registration history (existing and canceled)
        registrationRepository.findByUserIdAndEventId(user.getId(), event.getId())
                .ifPresent(registration -> {
                    if (registration.getStatus() == RegistrationStatus.CANCELED) {
                        throw new EventRegistrationException("Cannot register again after cancellation");
                    } else {
                        throw new EventRegistrationConflictException("User is already registered for this event");
                    }
                });

        // Validate user has appropriate role for event's target audience
        if (event.getAudience() != TargetAudience.BOTH) {
            boolean hasRequiredRole = userRoles.stream()
                    .anyMatch(role -> {
                        if (event.getAudience() == TargetAudience.STUDENT) {
                            return role.getName().equals("ROLE_STUDENT");
                        } else {
                            return role.getName().equals("ROLE_LECTURER");
                        }
                    });

            if (!hasRequiredRole) {
                throw new EventRegistrationException("User does not have appropriate role for this event");
            }
        }
    }

    private void validateRegistrationCapacity(Set<Role> userRoles, Event event) {
        // Create a map of role name to capacity for easier lookup
        Map<String, Integer> capacityMap = eventCapacityRepository.findByEvent(event).stream()
                .collect(Collectors.toMap(
                        capacity -> capacity.getRole().getName(),
                        EventCapacity::getCapacity));

        // Check capacity for each user role that has a defined capacity
        userRoles.stream()
                .map(Role::getName)
                .filter(capacityMap::containsKey)
                .forEach(roleName -> {
                    Long currentRegistrations = registrationRepository.countByEventAndUserRole(event, roleName);
                    if (currentRegistrations >= capacityMap.get(roleName)) {
                        throw new EventRegistrationException("Event capacity exceeded for " + roleName);
                    }
                });
    }

    private Registration createAndSaveRegistration(User user, Event event, String checkinUrl) {
        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setCheckinUrl(checkinUrl);
        registration.setStatus(RegistrationStatus.REGISTERED);
        return registrationRepository.save(registration);
    }

    private void validateRegistrationStatus(Registration registration) {
        if (registration.getStatus() == RegistrationStatus.CANCELED) {
            throw new EventRegistrationException("Registration has been canceled");
        } else if (registration.getStatus() == RegistrationStatus.ATTENDED) {
            throw new EventRegistrationException("Registration has already been checked in");
        } else if (registration.getStatus() != RegistrationStatus.REGISTERED) {
            throw new EventRegistrationException("Registration is not active");
        }
    }

    private void validateCheckinTime(Event event) {
        LocalDateTime now = LocalDateTime.now();
        if (event.getCheckinStart() != null && event.getCheckinEnd() != null) {
            if (now.isBefore(event.getCheckinStart())) {
                throw new EventRegistrationException("Check-in period has not started yet");
            }
            if (now.isAfter(event.getCheckinEnd())) {
                throw new EventRegistrationException("Check-in period has ended");
            }
        }
    }

    private void validateCheckinStaffRole(Long eventId) {
        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("User not authenticated");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        // Get the current user
        User staff = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Get the event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found")); // Get the EVENT_CHECKIN role
        StaffRole checkinRole = staffRoleRepository.findByStaffRoleName("EVENT_CHECKIN")
                .orElseThrow(
                        () -> new StaffRoleNotFoundException("Staff role EVENT_CHECKIN is not found in the system"));

        // Check if user is assigned as check-in staff for this event
        boolean isCheckinStaff = eventStaffRepository
                .findByEventAndStaffAndStaffRole(event, staff, checkinRole)
                .isPresent();

        if (!isCheckinStaff) {
            throw new AccessDeniedException("User is not authorized to perform check-in for this event");
        }
    }
}
