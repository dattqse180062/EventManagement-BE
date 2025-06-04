package swd392.eventmanagement.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import swd392.eventmanagement.model.dto.response.StaffRoleResponse;
import swd392.eventmanagement.model.entity.StaffRole;

import java.util.List;

/**
 * MapStruct mapper for converting between StaffRole entities and
 * StaffRoleResponse DTOs
 */
@Mapper(componentModel = "spring")
public interface StaffRoleMapper {

    /**
     * Convert a StaffRole entity to StaffRoleResponse DTO
     * 
     * @param staffRole The staff role entity to convert
     * @return A StaffRoleResponse DTO with staff role information
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "roleName", source = "staffRoleName")
    @Mapping(target = "description", source = "description")
    StaffRoleResponse toStaffRoleResponse(StaffRole staffRole);

    /**
     * Convert a list of StaffRole entities to a list of StaffRoleResponse DTOs
     * 
     * @param staffRoles The list of staff role entities to convert
     * @return A list of StaffRoleResponse DTOs
     */
    List<StaffRoleResponse> toStaffRoleResponseList(List<StaffRole> staffRoles);
}
