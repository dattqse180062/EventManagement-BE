package swd392.eventmanagement.service.registration.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import swd392.eventmanagement.exception.EventRegistrationException;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.enums.RegistrationStatus;
import swd392.eventmanagement.exception.AccessDeniedException;
import swd392.eventmanagement.exception.EventNotFoundException;
import swd392.eventmanagement.model.dto.request.RegistrationCreateRequest;
import swd392.eventmanagement.model.dto.response.CheckinResponse;
import swd392.eventmanagement.model.dto.response.RegistrationCancelResponse;
import swd392.eventmanagement.model.dto.response.RegistrationCreateResponse;
import swd392.eventmanagement.model.dto.response.RegistrationSearchResponse;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.Registration;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.model.mapper.RegistrationMapper;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.RegistrationRepository;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.service.registration.RegistrationService;
import swd392.eventmanagement.service.registration.builder.RegistrationBuilder;
import swd392.eventmanagement.service.registration.helper.CheckinCodeEncrypter;
import swd392.eventmanagement.service.registration.helper.RegistrationEmailSender;
import swd392.eventmanagement.service.registration.validator.AuthenticationValidator;
import swd392.eventmanagement.service.registration.validator.RegistrationValidator;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    // Core repositories
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    // Helper components
    private final RegistrationMapper registrationMapper;
    private final AuthenticationValidator authService;
    private final RegistrationValidator validationService;
    private final RegistrationBuilder builderService;
    private final RegistrationEmailSender emailService;
    private final CheckinCodeEncrypter encryptionService;

    @Override
    @Transactional
    public RegistrationCreateResponse createRegistration(RegistrationCreateRequest request) {
        logger.info("Creating new registration for event ID: {}", request.getEventId());

        // Authenticate user and get user entity
        User user = authService.authenticateAndGetUser(request.getEmail());

        // Get event entity
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + request.getEventId()));

        // Validate event registration status
        validationService.validateEventRegistrationStatus(event);

        // Validate user eligibility
        validationService.validateUserEligibility(user, event);

        // Validate registration capacity
        validationService.validateRegistrationCapacity(user.getRoles(), event);

        // Create and save registration
        Registration registration = builderService.createAndSaveRegistration(user, event);

        // Create response
        RegistrationCreateResponse response = registrationMapper.toRegistrationCreateResponse(registration);

        // Send confirmation email
        emailService.sendRegistrationConfirmationEmail(user, event, registration);

        logger.info("Successfully created registration ID: {} for event: {} and user: {}",
                registration.getId(), event.getId(), user.getId());

        return response;
    }

    @Override
    @Transactional
    public RegistrationCancelResponse cancelRegistration(Long eventId) {
        logger.info("Canceling registration for event ID: {}", eventId);

        // Get authenticated user
        User user = authService.getCurrentAuthenticatedUser();

        // Get event entity
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

        // Validate event cancellation status
        validationService.validateEventCancellationStatus(event);

        // Find registration
        Registration registration = registrationRepository.findByUserIdAndEventId(user.getId(), eventId)
                .orElseThrow(() -> new EventRegistrationException("Registration not found"));

        if (registration.getStatus() != RegistrationStatus.REGISTERED) {
            throw new EventRegistrationException("Registration cannot be canceled as it is not in REGISTERED status");
        }

        // Cancel registration
        Registration canceledRegistration = builderService.cancelRegistration(registration);

        // Send cancellation email
        emailService.sendRegistrationCancellationEmail(user, event, canceledRegistration);

        // Create response
        RegistrationCancelResponse response = registrationMapper.toRegistrationCancelResponse(canceledRegistration);

        logger.info("Successfully cancelled registration ID: {} for event: {} and user: {}",
                canceledRegistration.getId(), event.getId(), user.getId());

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
            validationService.validateCheckinStaffRole(eventId);

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
            validationService.validateCheckinStaffRole(eventId);

            // Get user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

            // Validate event
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

            // Find registration
            Registration registration = registrationRepository.findByUserIdAndEventId(user.getId(), eventId)
                    .orElseThrow(() -> new EventRegistrationException("Email not registered for this event")); // Check
                                                                                                               // registration
                                                                                                               // status
                                                                                                               // and
                                                                                                               // time
                                                                                                               // constraints
            validationService.validateRegistrationStatus(registration);
            validationService.validateCheckinTime(event);

            // Update registration status and checkin time
            Registration checkedInRegistration = builderService.checkinRegistration(registration);

            // Map to response using mapper
            CheckinResponse response = registrationMapper.toCheckinResponse(checkedInRegistration);

            logger.info("Successfully checked in - Registration ID: {}, User: {}, Event: {}",
                    checkedInRegistration.getId(), user.getEmail(), event.getId());

            return response;

        } catch (UserNotFoundException | EventNotFoundException | EventRegistrationException e) {
            logger.error("Check-in failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input parameters: {}", e.getMessage());
            throw e;
        } catch (AccessDeniedException e) {
            logger.error("Access denied for check-in: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during check-in: {}", e.getMessage(), e);
            throw new EventRegistrationException("An unexpected error occurred during check-in");
        }
    }

    @Override
    public CheckinResponse checkin(String checkinCode) {
        logger.info("Processing check-in using check-in code: {}", checkinCode);

        try {
            // Input validation
            if (checkinCode == null || checkinCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Check-in code is required");
            }

            // Decrypt the check-in code to get email and event ID
            String[] decryptedData = encryptionService.decryptEmailAndEventId(checkinCode);
            String email = decryptedData[0];
            Long eventId = Long.parseLong(decryptedData[1]);

            logger.info("Decrypted check-in data - Email: {}, Event ID: {}", email, eventId);

            // Use the existing checkin method with email and eventId
            return checkin(eventId, email);

        } catch (NumberFormatException e) {
            logger.error("Invalid event ID in check-in code: {}", e.getMessage());
            throw new EventRegistrationException("Invalid check-in code format");
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input parameters: {}", e.getMessage());
            throw e;
        }
    }
}
