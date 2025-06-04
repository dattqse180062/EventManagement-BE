package swd392.eventmanagement.service.event.validator;

import org.springframework.stereotype.Component;

import swd392.eventmanagement.exception.EventRequestValidationException;
import swd392.eventmanagement.exception.EventTypeNotFoundException;
import swd392.eventmanagement.model.dto.request.EventCreateRequest;

@Component
public class EventCreateValidator {

    private final EventCommonValidator commonValidator;
    private final RoleCapacityValidator roleCapacityValidator;
    private final EventModeValidator eventModeValidator;

    public EventCreateValidator(
            EventCommonValidator commonValidator,
            RoleCapacityValidator roleCapacityValidator,
            EventModeValidator eventModeValidator) {
        this.commonValidator = commonValidator;
        this.roleCapacityValidator = roleCapacityValidator;
        this.eventModeValidator = eventModeValidator;
    }

    /**
     * Validates the event create request
     * 
     * @param request Request to validate
     * @throws EventRequestValidationException If validation fails
     * @throws EventTypeNotFoundException      If event type doesn't exist
     */
    public void validateEventCreateRequest(EventCreateRequest request) {
        if (request == null) {
            throw new EventRequestValidationException("Event create request cannot be null");
        } // Validate common fields
        commonValidator.validateEventCommonFields(
                request.getName(),
                request.getAudience(),
                request.getMode(),
                request.getTypeId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getRegistrationStart(),
                request.getRegistrationEnd(),
                request.getMaxCapacity());

        // Validate mode requirements (location/platform)
        eventModeValidator.validateModeRequirements(
                request.getMode(),
                request.getLocation(),
                request.getPlatform());

        // Validate role capacities with target audience constraints
        roleCapacityValidator.validateRoleCapacities(request.getRoleCapacities(), request.getMaxCapacity(),
                request.getAudience());

    }
}
