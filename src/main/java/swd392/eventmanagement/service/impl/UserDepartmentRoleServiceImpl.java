package swd392.eventmanagement.service.impl;

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
}