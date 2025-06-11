package swd392.eventmanagement.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.exception.*;
import swd392.eventmanagement.model.dto.response.DepartmentRoleShowDTO;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.DepartmentRole;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.model.entity.UserDepartmentRole;
import swd392.eventmanagement.model.mapper.DepartmentRoleMapper;
import swd392.eventmanagement.repository.DepartmentRepository;
import swd392.eventmanagement.repository.DepartmentRoleRepository;
import swd392.eventmanagement.repository.UserDepartmentRoleRepository;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.service.UserDepartmentRoleService;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserDepartmentRoleServiceImpl implements UserDepartmentRoleService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentRoleRepository departmentRoleRepository;
    private final UserDepartmentRoleRepository userDepartmentRoleRepository;
    private final DepartmentRoleMapper departmentRoleMapper;
    private static final Logger logger = LoggerFactory.getLogger(UserDepartmentRoleServiceImpl.class);

    @Override
    public UserDepartmentRole assignRole(Long userId, Long departmentId, Long departmentRoleId) {
        logger.info("Assigning role ID: {} to user ID: {} in department ID: {}", departmentRoleId, userId, departmentId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new DepartmentNotFoundException("Department not found with ID: " + departmentId));

            DepartmentRole departmentRole = departmentRoleRepository.findById(departmentRoleId)
                    .orElseThrow(() -> new DepartmentRoleNotFoundException("Department role not found with ID: " + departmentRoleId));

            boolean exists = userDepartmentRoleRepository.existsByUserAndDepartmentAndDepartmentRole(user, department, departmentRole);

            if (exists) {
                throw new ValidationException("User already assigned to this department and role.");
            }

            UserDepartmentRole userDepartmentRole = new UserDepartmentRole();
            userDepartmentRole.setUser(user);
            userDepartmentRole.setDepartment(department);
            userDepartmentRole.setDepartmentRole(departmentRole);

            UserDepartmentRole saved = userDepartmentRoleRepository.save(userDepartmentRole);

            logger.info("Successfully assigned role ID: {} to user ID: {} in department ID: {}", departmentRoleId, userId, departmentId);

            return saved;
        } catch (ValidationException e) {
            logger.warn("Validation error during assigning role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during assigning role", e);
            throw new RuntimeException("Failed to assign role", e);
        }
    }
    @Override
    public List<DepartmentRoleShowDTO> getAllDepartmentRoles() {
        logger.info("Getting all department roles");
        try {
            List<DepartmentRole> departmentRoles = departmentRoleRepository.findAll();

            if (departmentRoles.isEmpty()) {
                logger.info("No department roles found");
                throw new DepartmentRoleNotFoundException("No department roles found");
            }

            logger.info("Found {} department roles", departmentRoles.size());
            return departmentRoleMapper.toDepartmentRoleShowDTOList(departmentRoles);
        } catch (DepartmentRoleNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving  department roles", e);
            throw new DepartmentRoleProcessingException("Failed to get department role", e);
        }
    }

    @Override
    public UserDepartmentRole updateUserDepartmentRole(Long userId, Long departmentId, Long newDepartmentRoleId) {
        logger.info("Updating role of user with ID: {} in department with ID: {} to new role with ID: {}", userId, departmentId, newDepartmentRoleId);

        try {
            // Find user by ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            // Find department by ID
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new DepartmentNotFoundException("Department not found with ID: " + departmentId));

            // Find new department role by ID
            DepartmentRole newRole = departmentRoleRepository.findById(newDepartmentRoleId)
                    .orElseThrow(() -> new DepartmentRoleNotFoundException("Department role not found with ID: " + newDepartmentRoleId));

            // Find existing relationship between user and department
            UserDepartmentRole existing = userDepartmentRoleRepository
                    .findByUserAndDepartment(user, department)
                    .orElseThrow(() -> new EntityNotFoundException("User has not been assigned to this department."));

            // If the user already has the same role, no update needed
            if (existing.getDepartmentRole().equals(newRole)) {
                throw new ValidationException("User already has this role in the department.");
            }

            // Check for conflict if the same user, department, and role combination already exists
            boolean conflict = userDepartmentRoleRepository.existsByUserAndDepartmentAndDepartmentRole(user, department, newRole);
            if (conflict) {
                throw new ValidationException("User has already been assigned this role in the department.");
            }

            // Update role
            existing.setDepartmentRole(newRole);
            return userDepartmentRoleRepository.save(existing);

        } catch (ValidationException e) {
            logger.warn("Validation error while updating user role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error occurred while updating user role in department", e);
            throw new DepartmentRoleProcessingException("Failed to update user role in department", e);
        }
    }

    @Override
    public void removeUserFromDepartment(Long userId, Long departmentId) {
        try {
            logger.info("Removing user {} from department {}", userId, departmentId);

            UserDepartmentRole relation = userDepartmentRoleRepository
                    .findByUserIdAndDepartmentId(userId, departmentId)
                    .orElseThrow(() -> {
                        logger.warn("User {} is not in department {}", userId, departmentId);
                        return new UserNotFoundException("User is not in this department");
                    });

            userDepartmentRoleRepository.delete(relation);

            logger.info("Successfully removed user {} from department {}", userId, departmentId);
        } catch (EntityNotFoundException e) {

            throw e;
        } catch (Exception e) {
            logger.error("Error occurred while removing user {} from department {}: {}", userId, departmentId, e.getMessage(), e);
            throw new DepartmentRoleProcessingException("Failed to remove user from department");
        }
    }
}