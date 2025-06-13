package swd392.eventmanagement.service.registration.validator;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.enums.RegistrationStatus;
import swd392.eventmanagement.enums.TargetAudience;
import swd392.eventmanagement.exception.AccessDeniedException;
import swd392.eventmanagement.exception.EventNotFoundException;
import swd392.eventmanagement.exception.EventRegistrationConflictException;
import swd392.eventmanagement.exception.EventRegistrationException;
import swd392.eventmanagement.exception.StaffRoleNotFoundException;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventCapacity;
import swd392.eventmanagement.model.entity.Registration;
import swd392.eventmanagement.model.entity.Role;
import swd392.eventmanagement.model.entity.StaffRole;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.EventCapacityRepository;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.EventStaffRepository;
import swd392.eventmanagement.repository.RegistrationRepository;
import swd392.eventmanagement.repository.StaffRoleRepository;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.security.service.UserDetailsImpl;

@Component
@RequiredArgsConstructor
public class RegistrationValidator {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationValidator.class);

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final EventCapacityRepository eventCapacityRepository;
    private final EventStaffRepository eventStaffRepository;
    private final StaffRoleRepository staffRoleRepository;

    /**
     * Validates that the event is open for registration
     */
    public void validateEventRegistrationStatus(Event event) {
        logger.info("Validating event registration status for event ID: {}", event.getId());

        if (event.getStatus() != EventStatus.PUBLISHED) {
            logger.error("Event {} is not published. Current status: {}", event.getId(), event.getStatus());
            throw new EventRegistrationException("Event is not open for registration");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getRegistrationStart()) || now.isAfter(event.getRegistrationEnd())) {
            logger.error(
                    "Registration for event {} is outside registration period. Current time: {}, Registration period: {} - {}",
                    event.getId(), now, event.getRegistrationStart(), event.getRegistrationEnd());
            throw new EventRegistrationException("Registration must be done within registration period");
        }

        logger.info("Event {} registration status validation passed", event.getId());
    }

    /**
     * Validates that the event is open for cancellation
     */
    public void validateEventCancellationStatus(Event event) {
        logger.info("Validating event cancellation status for event ID: {}", event.getId());

        if (event.getStatus() != EventStatus.PUBLISHED) {
            logger.error("Event {} is not published. Current status: {}", event.getId(), event.getStatus());
            throw new EventRegistrationException("Event is not open for registration");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getRegistrationStart()) || now.isAfter(event.getRegistrationEnd())) {
            logger.error(
                    "Cancellation for event {} is outside registration period. Current time: {}, Registration period: {} - {}",
                    event.getId(), now, event.getRegistrationStart(), event.getRegistrationEnd());
            throw new EventRegistrationException("Cancellation must be done within registration period");
        }

        // Check if cancellation is at least 1 day before event starts
        if (now.plusDays(1).isAfter(event.getStartTime())) {
            logger.error(
                    "Cancellation for event {} is too close to event start time. Current time: {}, Event starts: {}",
                    event.getId(), now, event.getStartTime());
            throw new EventRegistrationException("Cancellation must be done at least 1 day before event starts");
        }

        logger.info("Event {} cancellation status validation passed", event.getId());
    }

    /**
     * Validates user eligibility for event registration
     */
    public void validateUserEligibility(User user, Event event) {
        logger.info("Validating user eligibility for user ID: {} and event ID: {}", user.getId(), event.getId());

        Set<Role> userRoles = user.getRoles();
        logger.debug("User {} has roles: {}", user.getId(), userRoles.stream().map(Role::getName).toList());

        // Check registration history (existing and canceled)
        registrationRepository.findByUserIdAndEventId(user.getId(), event.getId())
                .ifPresent(registration -> {
                    if (registration.getStatus() == RegistrationStatus.CANCELED) {
                        logger.error(
                                "User {} attempted to re-register for event {} after cancellation. Registration ID: {}",
                                user.getId(), event.getId(), registration.getId());
                        throw new EventRegistrationException("Cannot register again after cancellation");
                    } else {
                        logger.error("User {} is already registered for event {}. Registration ID: {}, Status: {}",
                                user.getId(), event.getId(), registration.getId(), registration.getStatus());
                        throw new EventRegistrationConflictException("User is already registered for this event");
                    }
                });

        // Validate user has appropriate role for event's target audience
        if (event.getAudience() != TargetAudience.BOTH) {
            logger.debug("Event {} has target audience: {}, validating user roles", event.getId(), event.getAudience());

            boolean hasRequiredRole = userRoles.stream()
                    .anyMatch(role -> {
                        if (event.getAudience() == TargetAudience.STUDENT) {
                            return role.getName().equals("ROLE_STUDENT");
                        } else {
                            return role.getName().equals("ROLE_LECTURER");
                        }
                    });

            if (!hasRequiredRole) {
                logger.error("User {} does not have required role for event {} with target audience: {}",
                        user.getId(), event.getId(), event.getAudience());
                throw new EventRegistrationException("User does not have appropriate role for this event");
            }
        }

        logger.info("User eligibility validation passed for user ID: {} and event ID: {}", user.getId(), event.getId());
    }

    /**
     * Validates registration capacity for user roles
     */
    public void validateRegistrationCapacity(Set<Role> userRoles, Event event) {
        logger.info("Validating registration capacity for event ID: {} with user roles: {}",
                event.getId(), userRoles.stream().map(Role::getName).toList());

        // Create a map of role name to capacity for easier lookup
        Map<String, Integer> capacityMap = eventCapacityRepository.findByEvent(event).stream()
                .collect(Collectors.toMap(
                        capacity -> capacity.getRole().getName(),
                        EventCapacity::getCapacity));

        logger.debug("Event {} capacity settings: {}", event.getId(), capacityMap);

        // Check capacity for each user role that has a defined capacity
        userRoles.stream()
                .map(Role::getName)
                .filter(capacityMap::containsKey)
                .forEach(roleName -> {
                    Long currentRegistrations = registrationRepository.countByEventAndUserRole(event, roleName);
                    Integer maxCapacity = capacityMap.get(roleName);

                    logger.debug("Role {} - Current registrations: {}, Max capacity: {}",
                            roleName, currentRegistrations, maxCapacity);

                    if (currentRegistrations >= maxCapacity) {
                        logger.error("Event capacity exceeded for role {}. Current: {}, Max: {}",
                                roleName, currentRegistrations, maxCapacity);
                        throw new EventRegistrationException("Event capacity exceeded for " + roleName);
                    }
                });

        logger.info("Registration capacity validation passed for event ID: {}", event.getId());
    }

    /**
     * Validates registration status for check-in
     */
    public void validateRegistrationStatus(Registration registration) {
        logger.info("Validating registration status for registration ID: {}", registration.getId());

        if (registration.getStatus() == RegistrationStatus.CANCELED) {
            logger.error("Registration {} is canceled, cannot proceed with check-in", registration.getId());
            throw new EventRegistrationException("Registration has been canceled");
        } else if (registration.getStatus() == RegistrationStatus.ATTENDED) {
            logger.error("Registration {} is already checked in", registration.getId());
            throw new EventRegistrationException("Registration has already been checked in");
        } else if (registration.getStatus() != RegistrationStatus.REGISTERED) {
            logger.error("Registration {} has invalid status: {}", registration.getId(), registration.getStatus());
            throw new EventRegistrationException("Registration is not active");
        }

        logger.info("Registration status validation passed for registration ID: {}", registration.getId());
    }

    /**
     * Validates check-in time window
     */
    public void validateCheckinTime(Event event) {
        logger.info("Validating check-in time for event ID: {}", event.getId());

        LocalDateTime now = LocalDateTime.now();
        if (event.getCheckinStart() != null && event.getCheckinEnd() != null) {
            logger.debug("Event {} check-in window: {} - {}, Current time: {}",
                    event.getId(), event.getCheckinStart(), event.getCheckinEnd(), now);

            if (now.isBefore(event.getCheckinStart())) {
                logger.error("Check-in for event {} has not started yet. Start time: {}, Current time: {}",
                        event.getId(), event.getCheckinStart(), now);
                throw new EventRegistrationException("Check-in period has not started yet");
            }
            if (now.isAfter(event.getCheckinEnd())) {
                logger.error("Check-in for event {} has ended. End time: {}, Current time: {}",
                        event.getId(), event.getCheckinEnd(), now);
                throw new EventRegistrationException("Check-in period has ended");
            }
        } else {
            logger.debug("Event {} has no specific check-in time constraints", event.getId());
        }

        logger.info("Check-in time validation passed for event ID: {}", event.getId());
    }

    /**
     * Validates that the current user has check-in staff role for the event
     */
    public void validateCheckinStaffRole(Long eventId) {
        logger.info("Validating check-in staff role for event ID: {}", eventId);

        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.error("No authenticated user found for check-in staff validation");
            throw new AccessDeniedException("User not authenticated");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        logger.debug("Validating check-in staff role for user ID: {}", userDetails.getId());

        // Get the current user
        User staff = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> {
                    logger.error("Staff user not found in database with ID: {}", userDetails.getId());
                    return new UserNotFoundException("User not found");
                });

        // Get the event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    logger.error("Event not found with ID: {}", eventId);
                    return new EventNotFoundException("Event not found");
                });

        // Get the EVENT_CHECKIN role
        StaffRole checkinRole = staffRoleRepository.findByStaffRoleName("EVENT_CHECKIN")
                .orElseThrow(() -> {
                    logger.error("EVENT_CHECKIN staff role not found in system");
                    return new StaffRoleNotFoundException("Staff role EVENT_CHECKIN is not found in the system");
                });

        // Check if user is assigned as check-in staff for this event
        boolean isCheckinStaff = eventStaffRepository
                .findByEventAndStaffAndStaffRole(event, staff, checkinRole)
                .isPresent();

        if (!isCheckinStaff) {
            logger.error(
                    "User {} is not authorized for check-in on event {}. Missing EVENT_CHECKIN staff role assignment",
                    staff.getId(), eventId);
            throw new AccessDeniedException("User is not authorized to perform check-in for this event");
        }

        logger.info("Check-in staff role validation passed for user {} on event {}", staff.getId(), eventId);
    }

    /**
     * Validates input parameters
     */
    public void validateInputParameters(Long eventId, String email) {
        logger.info("Validating input parameters - Event ID: {}, Email: {}", eventId, email);

        if (eventId == null || email == null || email.trim().isEmpty()) {
            logger.error("Invalid input parameters - Event ID: {}, Email: {}", eventId, email);
            throw new IllegalArgumentException("Event ID and email are required");
        }

        logger.debug("Input parameters validation passed");
    }

    /**
     * Validates check-in code parameter
     */
    public void validateCheckinCode(String checkinCode) {
        logger.info("Validating check-in code parameter");

        if (checkinCode == null || checkinCode.trim().isEmpty()) {
            logger.error("Invalid check-in code: empty or null");
            throw new IllegalArgumentException("Check-in code is required");
        }

        logger.debug("Check-in code validation passed");
    }
}
