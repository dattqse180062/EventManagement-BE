package swd392.eventmanagement.service.event.validator;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import swd392.eventmanagement.enums.TargetAudience;
import swd392.eventmanagement.exception.EventRequestValidationException;
import swd392.eventmanagement.model.dto.request.RoleCapacityCreateRequest;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.repository.RoleRepository;
import swd392.eventmanagement.repository.RegistrationRepository;

@Component
public class RoleCapacityValidator {
    private static final Logger logger = LoggerFactory.getLogger(RoleCapacityValidator.class);

    private final RoleRepository roleRepository;

    public RoleCapacityValidator(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Validates role capacity settings with target audience constraints
     *
     * @param roleCapacities Set of role capacity settings
     * @param maxCapacity    Maximum overall capacity (optional)
     * @param audience       Target audience for the event
     * @throws EventRequestValidationException If any validation fails
     */
    public void validateRoleCapacities(Set<RoleCapacityCreateRequest> roleCapacities, Integer maxCapacity,
            TargetAudience audience) {

        // Check if role capacities are optional based on audience type
        if ((audience == TargetAudience.STUDENT || audience == TargetAudience.LECTURER)
                && (roleCapacities == null || roleCapacities.isEmpty())) {
            return;
        }

        // Role capacities are required for BOTH audience type
        if (audience == TargetAudience.BOTH && (roleCapacities == null || roleCapacities.isEmpty())) {
            throw new EventRequestValidationException("Role capacities are required for BOTH audience type");
        }

        if (roleCapacities == null || roleCapacities.isEmpty()) {
            return;
        }

        // Track which roles are included in capacities
        boolean hasStudentCapacity = false;
        boolean hasLecturerCapacity = false;

        // Validate each role capacity
        for (RoleCapacityCreateRequest roleCapacity : roleCapacities) {
            // Validate role name
            String roleName = roleCapacity.getRoleName();
            if (roleName == null || roleName.trim().isEmpty()) {
                throw new EventRequestValidationException("Role name cannot be empty in capacity settings");
            }

            // Validate capacity value
            if (roleCapacity.getMaxCapacity() == null || roleCapacity.getMaxCapacity() <= 0) {
                throw new EventRequestValidationException(
                        "Max capacity for role " + roleName + " must be greater than 0");
            }

            // Normalize role name and check if role exists
            String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            if (!roleRepository.findByName(normalizedRoleName).isPresent()) {
                logger.warn("Role with name {} does not exist in the database", normalizedRoleName);
            }

            // Track which roles are included
            if (normalizedRoleName.equals("ROLE_STUDENT")) {
                hasStudentCapacity = true;
            } else if (normalizedRoleName.equals("ROLE_LECTURER")) {
                hasLecturerCapacity = true;
            }
        }

        // Validate role combinations based on audience type
        if (audience == TargetAudience.BOTH && (!hasStudentCapacity || !hasLecturerCapacity)) {
            throw new EventRequestValidationException(
                    "Both student and lecturer role capacities must be specified for BOTH audience type");
        } else if (audience == TargetAudience.STUDENT && hasLecturerCapacity) {
            throw new EventRequestValidationException(
                    "Lecturer role capacity should not be specified for STUDENT audience type");
        } else if (audience == TargetAudience.LECTURER && hasStudentCapacity) {
            throw new EventRequestValidationException(
                    "Student role capacity should not be specified for LECTURER audience type");
        }

        // Validate total capacity matches max capacity if specified
        if (maxCapacity != null) {
            int totalCapacity = roleCapacities.stream()
                    .mapToInt(RoleCapacityCreateRequest::getMaxCapacity)
                    .sum();

            if (totalCapacity > maxCapacity) {
                throw new EventRequestValidationException("Sum of role capacities cannot exceed max capacity");
            }

            if (totalCapacity < maxCapacity) {
                throw new EventRequestValidationException("Sum of role capacities (" + totalCapacity +
                        ") must equal the max capacity (" + maxCapacity + ")");
            }
        }
    }

    /**
     * Validates that role capacities are not being reduced below the current number
     * of registrations
     * 
     * @param event                  Event entity to validate
     * @param roleCapacities         Requested role capacities
     * @param registrationRepository RegistrationRepository for counting current
     *                               registrations
     * @throws EventRequestValidationException If any capacity would be reduced
     *                                         below current registrations
     */
    public void validateRoleCapacitiesAgainstRegistrations(Event event,
            Set<RoleCapacityCreateRequest> roleCapacities,
            RegistrationRepository registrationRepository) {
        if (roleCapacities == null || roleCapacities.isEmpty()) {
            return;
        }
        for (RoleCapacityCreateRequest roleCapacity : roleCapacities) {
            String roleName = roleCapacity.getRoleName();
            String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            Long currentRegistrations = registrationRepository.countByEventAndUserRole(event, normalizedRoleName);
            int requestedCapacity = roleCapacity.getMaxCapacity();
            if (currentRegistrations != null && requestedCapacity < currentRegistrations) {
                throw new EventRequestValidationException(
                        "Cannot reduce capacity for role " + roleName + " below the current number of registrations ("
                                + currentRegistrations + ")");
            }
        }
    }

    /**
     * Validates that no registrations exist for roles being removed when changing
     * audience
     * 
     * @param event                  Event entity
     * @param newAudience            TargetAudience to update to
     * @param registrationRepository RegistrationRepository for checking
     *                               registrations
     * @throws EventRequestValidationException if there are still registrations for
     *                                         roles being removed
     */
    public void validateAudienceChange(Event event, TargetAudience newAudience,
            RegistrationRepository registrationRepository) {
        if (newAudience == TargetAudience.STUDENT) {
            Long lecturerCount = registrationRepository.countByEventAndUserRole(event, "ROLE_LECTURER");
            if (lecturerCount != null && lecturerCount > 0) {
                throw new EventRequestValidationException(
                        "Cannot change audience to STUDENT as there are existing lecturer registrations");
            }
        } else if (newAudience == TargetAudience.LECTURER) {
            Long studentCount = registrationRepository.countByEventAndUserRole(event, "ROLE_STUDENT");
            if (studentCount != null && studentCount > 0) {
                throw new EventRequestValidationException(
                        "Cannot change audience to LECTURER as there are existing student registrations");
            }
        }
    }
}
