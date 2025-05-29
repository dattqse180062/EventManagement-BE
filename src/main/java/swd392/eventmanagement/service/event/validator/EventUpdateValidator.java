package swd392.eventmanagement.service.event.validator;

import org.springframework.stereotype.Component;

import swd392.eventmanagement.exception.EventRequestValidationException;
import swd392.eventmanagement.exception.EventTypeNotFoundException;
import swd392.eventmanagement.model.dto.request.EventUpdateRequest;

@Component
public class EventUpdateValidator {

    private final EventCommonValidator commonValidator;
    private final RoleCapacityValidator roleCapacityValidator;
    private final EventModeValidator eventModeValidator;

    public EventUpdateValidator(
            EventCommonValidator commonValidator,
            RoleCapacityValidator roleCapacityValidator,
            EventModeValidator eventModeValidator) {
        this.commonValidator = commonValidator;
        this.roleCapacityValidator = roleCapacityValidator;
        this.eventModeValidator = eventModeValidator;
    }

    /**
     * Validates all aspects of an event update request including capacity
     * constraints
     * 
     * @param request The EventUpdateRequest to validate
     * @throws EventRequestValidationException if validation fails
     * @throws EventTypeNotFoundException      if referenced event type doesn't
     *                                         exist
     */
    public void validateEventUpdateRequest(EventUpdateRequest request) {
        if (request == null) {
            throw new EventRequestValidationException("Event update request cannot be null");
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
