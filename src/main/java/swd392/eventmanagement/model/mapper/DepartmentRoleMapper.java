package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import swd392.eventmanagement.model.dto.response.DepartmentRoleShowDTO;
import swd392.eventmanagement.model.entity.DepartmentRole;


import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentRoleMapper {

    DepartmentRoleMapper INSTANCE = Mappers.getMapper(DepartmentRoleMapper.class);

    DepartmentRoleShowDTO toDepartmentRoleShowDTO(DepartmentRole departmentRole);

    List<DepartmentRoleShowDTO> toDepartmentRoleShowDTOList(List<DepartmentRole> departmentRoles);
}
