package swd392.eventmanagement.model.dto.response;

import lombok.Data;

@Data
public class EventTypeDistribution {
    private String eventType;
    private long count;
}
