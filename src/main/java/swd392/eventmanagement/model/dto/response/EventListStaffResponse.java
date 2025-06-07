package swd392.eventmanagement.model.dto.response;

import java.util.Set;

import lombok.Data;

@Data
public class EventListStaffResponse {
    private EventListAvailableResponse eventInfo;
    private Set<String> staffRoles;
}
