package swd392.eventmanagement.service.event;

import java.util.List;

import swd392.eventmanagement.model.dto.request.StaffManageRequest;
import swd392.eventmanagement.model.dto.response.StaffResponse;
import swd392.eventmanagement.model.dto.response.StaffRoleResponse;

public interface StaffService {
    List<StaffRoleResponse> getAllStaffRoles();

    List<StaffResponse> getEventStaffs(Long eventId, String departmentCode);

    StaffResponse assignStaff(Long eventId, String departmentCode, StaffManageRequest staffCreateRequest);

    StaffResponse updateStaff(Long eventId, String departmentCode, StaffManageRequest staffUpdateRequest);

    void removeStaff(Long eventId, String departmentCode, String staffEmail);
}
