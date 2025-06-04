package swd392.eventmanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.exception.DepartmentNotFoundException;
import swd392.eventmanagement.exception.DepartmentRoleNotFoundException;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.DepartmentRole;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.model.entity.UserDepartmentRole;
import swd392.eventmanagement.repository.DepartmentRepository;
import swd392.eventmanagement.repository.DepartmentRoleRepository;
import swd392.eventmanagement.repository.UserDepartmentRoleRepository;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.service.UserDepartmentRoleService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDepartmentRoleServiceImpl implements UserDepartmentRoleService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentRoleRepository departmentRoleRepository;
    private final UserDepartmentRoleRepository userDepartmentRoleRepository;

    @Override
    public UserDepartmentRole assignRole(Long userId, Long departmentId, Long departmentRoleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

        DepartmentRole departmentRole = departmentRoleRepository.findById(departmentRoleId)
                .orElseThrow(() -> new DepartmentRoleNotFoundException("Department role not found"));

        // Lấy tất cả các bản ghi liên quan tới user này
        List<UserDepartmentRole> userRoles = userDepartmentRoleRepository.findAll();

        // Kiểm tra xem đã tồn tại tổ hợp rồi chưa
        boolean exists = userRoles.stream().anyMatch(udr ->
                udr.getUser().getId().equals(userId)
                        && udr.getDepartment().getId().equals(departmentId)
                        && udr.getDepartmentRole().getId().equals(departmentRoleId)
        );

        if (exists) {
            throw new IllegalStateException("User already assigned to this department and role.");
        }

        // Nếu chưa có, tạo mới
        UserDepartmentRole userDepartmentRole = new UserDepartmentRole();
        userDepartmentRole.setUser(user);
        userDepartmentRole.setDepartment(department);
        userDepartmentRole.setDepartmentRole(departmentRole);

        return userDepartmentRoleRepository.save(userDepartmentRole);
    }
}