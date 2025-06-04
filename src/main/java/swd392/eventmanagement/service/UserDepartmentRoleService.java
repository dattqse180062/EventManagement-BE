package swd392.eventmanagement.service;

import swd392.eventmanagement.model.entity.UserDepartmentRole;

public interface UserDepartmentRoleService {
    UserDepartmentRole assignRole(Long userId, Long departmentId, Long departmentRoleId);
}
