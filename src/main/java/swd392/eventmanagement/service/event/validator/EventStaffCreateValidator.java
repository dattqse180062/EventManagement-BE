package swd392.eventmanagement.service.event.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import swd392.eventmanagement.exception.DuplicateResourceException;
import swd392.eventmanagement.exception.EventStaffNotFoundException;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventStaff;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.EventStaffRepository;

import java.util.List;

@Component
public class EventStaffCreateValidator {
    private static final Logger logger = LoggerFactory.getLogger(EventStaffCreateValidator.class);
    private final EventStaffRepository eventStaffRepository;

    public EventStaffCreateValidator(EventStaffRepository eventStaffRepository) {
        this.eventStaffRepository = eventStaffRepository;
    }

    /**
     * Validates that a staff member is not already assigned to an event.
     * 
     * @param event The event entity to check
     * @param user  The user entity to check
     * @throws DuplicateResourceException if staff is already assigned to the event
     */
    public void validateEventStaffNotExist(Event event, User user) {
        if (eventStaffRepository.existsByEventAndStaff(event, user)) {
            throw new DuplicateResourceException(
                    String.format("Staff with ID %d is already assigned to event with ID %d", user.getId(),
                            event.getId()));
        }
    }

    /**
     * Validates that a staff member is assigned to an event.
     * 
     * @param event     The event entity to check
     * @param user      The user entity to check
     * @param userEmail User's email for error message (optional, can be null)
     * @throws EventStaffNotFoundException if staff is not assigned to the event
     */
    public void validateEventStaffExists(Event event, User user, String userEmail) {
        List<EventStaff> existingEventStaff = eventStaffRepository.findByEventAndStaff(event, user);

        if (existingEventStaff.isEmpty()) {
            String email = userEmail != null ? userEmail : user.getEmail();
            logger.warn("No staff assignment found for Staff ID: {}, Event ID: {}",
                    user.getId(), event.getId());
            throw new EventStaffNotFoundException(
                    "Staff member with email " + email + " is not assigned to event with ID " + event.getId());
        }
    }
}
