package swd392.eventmanagement.service.impl;

import java.time.LocalDateTime;
import java.util.Set;

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
import swd392.eventmanagement.model.dto.request.RegistrationCreateRequest;
import swd392.eventmanagement.model.dto.response.RegistrationCreateResponse;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventCapacity;
import swd392.eventmanagement.model.entity.Registration;
import swd392.eventmanagement.model.entity.Role;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.EventCapacityRepository;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.RegistrationRepository;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.security.service.UserDetailsImpl;
import swd392.eventmanagement.service.RegistrationService;
import swd392.eventmanagement.model.mapper.RegistrationMapper;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final EventCapacityRepository eventCapacityRepository;
    private final RegistrationMapper registrationMapper;

    @Override
    @Transactional
    public RegistrationCreateResponse createRegistration(RegistrationCreateRequest request) {
        logger.info("Creating new registration for event ID: {}", request.getEventId());

        User user = authenticateAndGetUser(request);
        Event event = validateEvent(request.getEventId());
        validateUserEligibility(user, event);
        validateRegistrationCapacity(user.getRoles(), event);

        Registration registration = createAndSaveRegistration(user, event, request.getCheckinUrl());
        RegistrationCreateResponse response = registrationMapper.toRegistrationCreateResponse(registration);

        logger.info("Successfully created registration ID: {} for event: {} and user: {}",
                registration.getId(), event.getId(), user.getId());

        return response;
    }

    private User authenticateAndGetUser(RegistrationCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("User not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        if (!userDetails.getEmail().equals(request.getEmail())) {
            throw new EventRegistrationException("User email does not match authenticated user");
        }

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Event validateEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventRegistrationException("Event is not open for registration");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getRegistrationStart())) {
            throw new EventRegistrationException("Registration has not started yet");
        }
        if (now.isAfter(event.getRegistrationEnd())) {
            throw new EventRegistrationException("Registration deadline has passed");
        }

        return event;
    }

    private void validateUserEligibility(User user, Event event) {
        Set<Role> userRoles = user.getRoles();

        // Check for duplicate registration
        if (registrationRepository.findByUserIdAndEventId(user.getId(), event.getId()).isPresent()) {
            throw new EventRegistrationConflictException("User is already registered for this event");
        }

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
        for (Role userRole : userRoles) {
            for (EventCapacity capacity : eventCapacityRepository.findByEvent(event)) {
                if (userRole.getName().equals(capacity.getRole().getName())) {
                    Long currentRegistrations = registrationRepository.countByEventAndUserRole(event,
                            userRole.getName());
                    if (currentRegistrations >= capacity.getCapacity()) {
                        throw new EventRegistrationException("Event capacity exceeded for " + userRole.getName());
                    }
                }
            }
        }
    }

    private Registration createAndSaveRegistration(User user, Event event, String checkinUrl) {
        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setCheckinUrl(checkinUrl);
        registration.setStatus(RegistrationStatus.REGISTERED);
        return registrationRepository.save(registration);
    }
}
