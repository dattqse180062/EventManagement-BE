package swd392.eventmanagement.model.dto.response;

import lombok.Data;
import swd392.eventmanagement.enums.EventStatus;

@Data
public class EventUpdateStatusResponse {
    private Long eventId;
    private EventStatus previousStatus;
    private EventStatus currentStatus;
}
