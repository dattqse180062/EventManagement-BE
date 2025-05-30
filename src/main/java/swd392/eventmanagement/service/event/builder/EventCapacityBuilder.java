package swd392.eventmanagement.service.event.builder;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import swd392.eventmanagement.enums.TargetAudience;
import swd392.eventmanagement.exception.EventRequestValidationException;
import swd392.eventmanagement.model.dto.request.RoleCapacityCreateRequest;
import swd392.eventmanagement.model.dto.response.EventDetailsManagementDTO;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventCapacity;
import swd392.eventmanagement.model.entity.Role;
import swd392.eventmanagement.repository.EventCapacityRepository;
import swd392.eventmanagement.repository.RegistrationRepository;
import swd392.eventmanagement.repository.RoleRepository;
import swd392.eventmanagement.service.event.validator.RoleCapacityValidator;

@Component
public class EventCapacityBuilder {

    private final EventCapacityRepository eventCapacityRepository;
    private final RegistrationRepository registrationRepository;
    private final RoleRepository roleRepository;
    private final RoleCapacityValidator roleCapacityValidator;

    public EventCapacityBuilder(
            EventCapacityRepository eventCapacityRepository,
            RegistrationRepository registrationRepository,
            RoleRepository roleRepository,
            RoleCapacityValidator roleCapacityValidator) {
        this.eventCapacityRepository = eventCapacityRepository;
        this.registrationRepository = registrationRepository;
        this.roleRepository = roleRepository;
        this.roleCapacityValidator = roleCapacityValidator;
    }

    /**
     * Updates the role capacities for an event
     * Using PUT semantics - removes all existing capacities and adds new ones if
     * provided
     * 
     * @param event          Event entity to update
     * @param roleCapacities List of role capacities to associate with the event,
     *                       can be null or empty
     */
    public void updateEventCapacities(Event event, Set<RoleCapacityCreateRequest> roleCapacities) {
        // Check if registration exists when switching audience
        roleCapacityValidator.validateAudienceChange(event, event.getAudience(), registrationRepository);
        // First, validate that we're not reducing any capacity below the current
        // registrations
        roleCapacityValidator.validateRoleCapacitiesAgainstRegistrations(event, roleCapacities, registrationRepository);

        // Remove all existing capacities first
        List<EventCapacity> existingCapacities = eventCapacityRepository.findByEvent(event);
        eventCapacityRepository.deleteAll(existingCapacities);

        // Create new capacities based on audience and provided role capacities
        createEventCapacities(event, roleCapacities, event.getAudience(), event.getMaxCapacity());
    }

    /**
     * Creates event capacities for different roles based on target audience
     * 
     * @param event          The event entity
     * @param roleCapacities Set of role capacities to create
     * @param audience       Target audience of the event
     * @param maxCapacity    Maximum overall capacity
     */
    public void createEventCapacities(Event event,
            Set<RoleCapacityCreateRequest> roleCapacities,
            TargetAudience audience,
            Integer maxCapacity) {

        // If no role capacities provided but we know the audience, create default
        // capacities
        if ((roleCapacities == null || roleCapacities.isEmpty()) && audience != null) {
            if (audience == TargetAudience.STUDENT) {
                // Create a single capacity for students with the full max capacity
                Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                        .orElseThrow(() -> new EventRequestValidationException("Student role not found"));
                createSingleCapacity(event, studentRole, maxCapacity);
            } else if (audience == TargetAudience.LECTURER) {
                // Create a single capacity for lecturers with the full max capacity
                Role lecturerRole = roleRepository.findByName("ROLE_LECTURER")
                        .orElseThrow(() -> new EventRequestValidationException("Lecturer role not found"));
                createSingleCapacity(event, lecturerRole, maxCapacity);
            }
            return; // Don't continue to create explicit capacities since we've handled the default
                    // case
        }

        // Handle explicit role capacities if provided
        if (roleCapacities != null && !roleCapacities.isEmpty()) {
            createEventCapacities(event, roleCapacities);
        }
    }

    /**
     * Creates a single capacity entry for an event role
     */
    private void createSingleCapacity(Event event, Role role, Integer maxCapacity) {
        EventCapacity eventCapacity = new EventCapacity();

        // Initialize the composite ID
        EventCapacity.EventCapacityId id = new EventCapacity.EventCapacityId();
        id.setEventId(event.getId());
        id.setRoleId(role.getId());
        eventCapacity.setId(id);

        eventCapacity.setEvent(event);
        eventCapacity.setRole(role);
        eventCapacity.setCapacity(maxCapacity != null ? maxCapacity : 0);

        eventCapacityRepository.save(eventCapacity);
    }

    /**
     * Creates event capacities for different roles
     */
    public void createEventCapacities(Event event,
            Set<RoleCapacityCreateRequest> roleCapacities) {
        for (RoleCapacityCreateRequest roleCapacityRequest : roleCapacities) {
            // Prefix role name with "ROLE_" if not already prefixed
            String roleName = roleCapacityRequest.getRoleName();
            String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            Role role = roleRepository.findByName(normalizedRoleName)
                    .orElseThrow(
                            () -> new EventRequestValidationException(
                                    "Role not found with name: " + normalizedRoleName));

            EventCapacity eventCapacity = new EventCapacity();
            // Initialize the composite ID
            EventCapacity.EventCapacityId id = new EventCapacity.EventCapacityId();
            id.setEventId(event.getId());
            id.setRoleId(role.getId());
            eventCapacity.setId(id);

            eventCapacity.setEvent(event);
            eventCapacity.setRole(role);
            eventCapacity.setCapacity(roleCapacityRequest.getMaxCapacity());

            eventCapacityRepository.save(eventCapacity);
        }
    }

    /**
     * Sets capacity information for the event DTO
     */
    public void setEventCapacityInfo(EventDetailsManagementDTO dto, Event event) {
        for (EventCapacity capacity : eventCapacityRepository.findByEvent(event)) {
            String roleName = capacity.getRole().getName();
            // Check both with and without the ROLE_ prefix
            if ("ROLE_STUDENT".equals(roleName)) {
                dto.setMaxCapacityStudent(capacity.getCapacity());
            } else if ("ROLE_LECTURER".equals(roleName)) {
                dto.setMaxCapacityLecturer(capacity.getCapacity());
            }
        }
    }
}
