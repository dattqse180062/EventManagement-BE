package swd392.eventmanagement.service;

import swd392.eventmanagement.model.dto.response.DepartmentRoleShowDTO;
import swd392.eventmanagement.model.entity.UserDepartmentRole;

import java.util.List;

public interface UserDepartmentRoleService {
    UserDepartmentRole assignRole(Long userId, Long departmentId, Long departmentRoleId);

    List<DepartmentRoleShowDTO> getAllDepartmentRoles();

    public UserDepartmentRole updateUserDepartmentRole(Long userId, Long departmentId, Long newDepartmentRoleId);
}
