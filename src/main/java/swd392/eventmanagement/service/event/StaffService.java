package swd392.eventmanagement.service.event;

import swd392.eventmanagement.model.dto.request.StaffCreateRequest;
import swd392.eventmanagement.model.dto.response.StaffResponse;

public interface StaffService {
    StaffResponse assignStaff(Long eventId, String departmentCode, StaffCreateRequest staffCreateRequest);
}
