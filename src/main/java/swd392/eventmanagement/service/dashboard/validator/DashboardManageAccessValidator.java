package swd392.eventmanagement.service.dashboard.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import swd392.eventmanagement.exception.AccessDeniedException;
import swd392.eventmanagement.exception.DepartmentNotFoundException;
import swd392.eventmanagement.exception.EventNotFoundException;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.DepartmentRole;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.*;
import swd392.eventmanagement.security.service.UserDetailsImpl;
import swd392.eventmanagement.service.survey.validator.SurveyManageAccessValidator;

@Component
public class DashboardManageAccessValidator {

    private static final Logger logger = LoggerFactory.getLogger(SurveyManageAccessValidator.class);

    private final DepartmentRepository departmentRepository;
    private final UserDepartmentRoleRepository userDepartmentRoleRepository;
    private final DepartmentRoleRepository departmentRoleRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public  DashboardManageAccessValidator(
            DepartmentRepository departmentRepository,
            UserDepartmentRoleRepository userDepartmentRoleRepository,
            DepartmentRoleRepository departmentRoleRepository,
            UserRepository userRepository,
            EventRepository eventRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.userDepartmentRoleRepository = userDepartmentRoleRepository;
        this.departmentRoleRepository = departmentRoleRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;

    }


    /**
     * Validates user authentication and department access permissions
     *
     * @param departmentCode Code of the department to check permissions for
     * @return Department entity if access is granted
     * @throws AccessDeniedException       If user lacks permissions
     * @throws DepartmentNotFoundException If department doesn't exist
     */
    public Department validateUserDepartmentAccess(String departmentCode) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Access denied. Authentication required.");
        }

        // Get principal and fetch user from DB
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Check if user is ADMIN
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role.getName()));
        if (isAdmin) {
            return departmentRepository.findByCode(departmentCode)
                    .orElseThrow(() -> new DepartmentNotFoundException("No department found with code: " + departmentCode));
        }

        // Check department
        Department department = departmentRepository.findByCode(departmentCode)
                .orElseThrow(() -> new DepartmentNotFoundException("No department found with code: " + departmentCode));

        if (!Boolean.TRUE.equals(department.getIsActive())) {
            logger.warn("Attempted access to inactive department with code: {}", departmentCode);
            throw new AccessDeniedException("Access denied. Department is inactive.");
        }

        // Check if user is HEAD of department
        if (!isHeadOfDepartment(user, department)) {
            throw new AccessDeniedException("Access denied. Only department HEAD or ADMIN can access this resource.");
        }

        return department;
    }

    /**
     * Checks if the current authenticated user is the HEAD of the specified
     * department.
     *
     * @param department The department to check
     * @return true if the user is the HEAD of the department, false otherwise
     */
    public boolean isHeadOfDepartment(User user, Department department) {
        try {
            // Find the HEAD role from department roles
            DepartmentRole headRole = departmentRoleRepository.findByName("HEAD")
                    .orElseThrow(() -> new RuntimeException("Department role 'HEAD' not found"));

            // Check if the user has the HEAD role in the specified department
            return userDepartmentRoleRepository.findByUserAndDepartmentAndDepartmentRole(user, department, headRole)
                    .isPresent();

        } catch (Exception e) {
            logger.error("Error checking if user is HEAD of department: {}", department.getCode(), e);
            return false;
        }
    }

    public void validateEventBelongsToUserDepartment(Long eventId, String departmentCode) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Not found event with id: " + eventId));

        if (!event.getDepartment().getCode().equals(departmentCode)) {
            throw new AccessDeniedException("You do not have permission to operate on an event that does not belong to your department.");
        }
    }
}
