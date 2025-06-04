package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import swd392.eventmanagement.model.dto.response.UserDepartmentRoleDTO;
import swd392.eventmanagement.model.entity.UserDepartmentRole;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserDepartmentRoleMapper {
    UserDepartmentRoleMapper INSTANCE = Mappers.getMapper(UserDepartmentRoleMapper.class);

    @Mapping(target = "departmentCode", source = "department.code")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "roleName", source = "departmentRole.name")
    UserDepartmentRoleDTO toDTO(UserDepartmentRole userDepartmentRole);

    Set<UserDepartmentRoleDTO> toDTOSet(Set<UserDepartmentRole> userDepartmentRoles);
}
