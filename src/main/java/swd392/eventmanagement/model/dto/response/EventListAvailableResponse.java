package swd392.eventmanagement.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.enums.TargetAudience;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventListAvailableResponse {
    private Long id;
    private String name;

    private String departmentName;

    private String typeName;
    private TargetAudience audience;
    private EventStatus status;

    private String locationAddress;
    private String locationAddress2;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String posterUrl;
    private String bannerUrl;
    private String description;

    private EventMode mode;

    @Builder.Default
    private Set<TagShowDTO> tags = new HashSet<>();
}
