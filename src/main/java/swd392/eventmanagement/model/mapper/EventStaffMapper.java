package swd392.eventmanagement.model.mapper;

import java.util.Collections;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import swd392.eventmanagement.model.dto.response.StaffResponse;
import swd392.eventmanagement.model.entity.EventStaff;

@Mapper(componentModel = "spring")
public interface EventStaffMapper {
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "staffName", source = "staff.fullName")
    @Mapping(target = "email", source = "staff.email")
    @Mapping(target = "roleName", source = "staffRole.staffRoleName", qualifiedByName = "toRoleNameSet")
    StaffResponse toStaffResponse(EventStaff eventStaff);

    /**
     * Convert a single role name to a set of roles
     */
    @Named("toRoleNameSet")
    default Set<String> toRoleNameSet(String roleName) {
        return roleName != null ? Collections.singleton(roleName) : Collections.emptySet();
    }
}
