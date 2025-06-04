package swd392.eventmanagement.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.enums.TargetAudience;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventListManagementDTO {
    private Long id;
    private String name;

    private String typeName;
    private TargetAudience audience;

    private String locationAddress;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private EventMode mode;

    private String posterUrl;
    private String bannerUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private EventStatus status;
}
