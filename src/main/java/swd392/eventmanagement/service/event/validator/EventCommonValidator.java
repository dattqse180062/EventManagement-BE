package swd392.eventmanagement.service.event.validator;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.TargetAudience;
import swd392.eventmanagement.exception.EventRequestValidationException;
import swd392.eventmanagement.exception.EventTypeNotFoundException;
import swd392.eventmanagement.repository.EventTypeRepository;

@Component
public class EventCommonValidator {

    private final EventTypeRepository eventTypeRepository;

    public EventCommonValidator(EventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    /**
     * Common validation for event requests
     *
     * @param name              Event name
     * @param typeId            Event type ID
     * @param startTime         Event start time
     * @param endTime           Event end time
     * @param registrationStart Event registration start time (optional)
     * @param registrationEnd   Event registration end time (optional)
     * @param maxCapacity       Event maximum capacity (optional)
     * @throws EventRequestValidationException If any validation fails
     * @throws EventTypeNotFoundException      If event type doesn't exist
     */
    public void validateEventCommonFields(
            String name,
            TargetAudience audience,
            EventMode mode,
            Long typeId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            LocalDateTime registrationStart,
            LocalDateTime registrationEnd,
            Integer maxCapacity) {

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new EventRequestValidationException("Event name cannot be null or empty");
        }

        // Validate audience
        if (audience == null) {
            throw new EventRequestValidationException("Target audience cannot be null");
        }

        // Validate mode
        if (mode == null) {
            throw new EventRequestValidationException("Event mode cannot be null");
        }

        // Validate type ID
        if (typeId == null) {
            throw new EventRequestValidationException("Event type ID cannot be null");
        }

        // Check if the event type exists in the database
        if (!eventTypeRepository.existsById(typeId)) {
            throw new EventTypeNotFoundException("Event type not found with id: " + typeId);
        }

        // Validate times
        if (startTime == null) {
            throw new EventRequestValidationException("Start time cannot be null");
        }

        if (endTime == null) {
            throw new EventRequestValidationException("End time cannot be null");
        }

        if (startTime.isAfter(endTime)) {
            throw new EventRequestValidationException("Start time cannot be after end time");
        }

        // Check if start time is in the past
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new EventRequestValidationException("Start time cannot be in the past");
        }

        // Validate registration times if provided
        if (registrationStart != null && registrationEnd != null) {
            if (registrationStart.isAfter(registrationEnd)) {
                throw new EventRequestValidationException(
                        "Registration start time cannot be after registration end time");
            }

            if (registrationEnd.isAfter(startTime)) {
                throw new EventRequestValidationException("Registration end time must be before event start time");
            }
        }

        // Validate max capacity if provided
        if (maxCapacity != null && maxCapacity <= 0) {
            throw new EventRequestValidationException("Max capacity must be greater than 0");
        }
    }
}
