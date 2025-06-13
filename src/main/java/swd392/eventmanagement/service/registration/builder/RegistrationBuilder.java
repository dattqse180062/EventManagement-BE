package swd392.eventmanagement.service.registration.builder;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import swd392.eventmanagement.enums.RegistrationStatus;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.Registration;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.RegistrationRepository;
import swd392.eventmanagement.service.registration.helper.CheckinCodeEncrypter;

@Component
@RequiredArgsConstructor
public class RegistrationBuilder {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationBuilder.class);

    private final RegistrationRepository registrationRepository;
    private final CheckinCodeEncrypter encryptionService;

    /**
     * Creates and saves a new registration
     */
    public Registration createAndSaveRegistration(User user, Event event) {
        logger.info("Creating registration for user {} and event {}", user.getId(), event.getId());

        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setCheckinCode(encryptionService.encryptEmailAndEventId(user.getEmail(), event.getId()));
        registration.setStatus(RegistrationStatus.REGISTERED);

        Registration savedRegistration = registrationRepository.save(registration);

        logger.info("Created registration with ID: {}", savedRegistration.getId());
        return savedRegistration;
    }

    /**
     * Updates registration status to canceled
     */
    public Registration cancelRegistration(Registration registration) {
        logger.info("Canceling registration with ID: {}", registration.getId());

        registration.setStatus(RegistrationStatus.CANCELED);
        registration.setCanceledAt(LocalDateTime.now());

        Registration savedRegistration = registrationRepository.save(registration);

        logger.info("Canceled registration with ID: {}", savedRegistration.getId());
        return savedRegistration;
    }

    /**
     * Updates registration status to attended (check-in)
     */
    public Registration checkinRegistration(Registration registration) {
        logger.info("Checking in registration with ID: {}", registration.getId());

        LocalDateTime now = LocalDateTime.now();
        registration.setStatus(RegistrationStatus.ATTENDED);
        registration.setCheckinAt(now);

        Registration savedRegistration = registrationRepository.save(registration);

        logger.info("Checked in registration with ID: {}", savedRegistration.getId());
        return savedRegistration;
    }
}
