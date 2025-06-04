package swd392.eventmanagement.model.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Convert a list of EventStaff to list of StaffResponse,
     * grouping by staff and combining their roles
     */
    @Named("toFullStaffResponse")
    @Mapping(target = "eventName", source = "firstEventStaff.event.name")
    @Mapping(target = "staffName", source = "firstEventStaff.staff.fullName")
    @Mapping(target = "email", source = "firstEventStaff.staff.email")
    @Mapping(target = "roleName", source = "staffRoles")
    @Mapping(target = "assignedAt", source = "firstEventStaff.assignedAt")
    @Mapping(target = "updatedAt", source = "firstEventStaff.updatedAt")
    StaffResponse toFullStaffResponse(EventStaff firstEventStaff, Set<String> staffRoles);

    /**
     * Convert a list of EventStaff records to a list of StaffResponse,
     * with combined roles and correct timestamps for each staff member.
     * This will be implemented by MapStruct
     */
    List<StaffResponse> toStaffResponseList(List<EventStaff> staffs);

    /**
     * Default implementation for processing the staff list
     */
    @Named("processStaffList")
    default List<StaffResponse> processEventStaffList(List<EventStaff> staffs) {
        return staffs.stream()
                .collect(Collectors.groupingBy(EventStaff::getStaff))
                .values()
                .stream()
                .map(staffGroup -> {
                    Set<String> roles = staffGroup.stream()
                            .map(es -> es.getStaffRole().getStaffRoleName())
                            .collect(Collectors.toSet());
                    return toFullStaffResponse(staffGroup.get(0), roles);
                })
                .collect(Collectors.toList());
    }
}
