package swd392.eventmanagement.service.event.validator;

import org.springframework.stereotype.Component;
import swd392.eventmanagement.exception.DuplicateResourceException;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.EventStaffRepository;

@Component
public class EventStaffCreateValidator {
    private final EventStaffRepository eventStaffRepository;

    public EventStaffCreateValidator(EventStaffRepository eventStaffRepository) {
        this.eventStaffRepository = eventStaffRepository;
    }

    public void validateEventStaffNotExist(Event event, User user) {
        if (eventStaffRepository.existsByEventAndStaff(event, user)) {
            throw new DuplicateResourceException(
                    String.format("Staff with ID %d is already assigned to event with ID %d", user.getId(),
                            event.getId()));
        }
    }
}
