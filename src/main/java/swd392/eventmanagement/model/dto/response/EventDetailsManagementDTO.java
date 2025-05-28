package swd392.eventmanagement.model.dto.response;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.enums.TargetAudience;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailsManagementDTO {
    // Basic event information
    private Long id;
    private String name;
    private String description;
    private EventStatus status;

    // Event type information
    private Long typeId;
    private String typeName;

    // Audience and capacity
    private TargetAudience audience;
    private Integer maxCapacity;
    private Integer maxCapacityStudent;
    private Integer maxCapacityLecturer;

    // Location information
    private LocationDTO location;

    // Event timing
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;

    // Media content
    private String posterUrl;
    private String bannerUrl;

    // Event mode and platform
    private EventMode mode;
    private PlatformDTO platform;

    // Tags associated with the event
    @Builder.Default
    private Set<TagShowDTO> tags = new HashSet<>();

    // Event images
    @Builder.Default
    private Set<ImageDTO> images = new HashSet<>();

    // Survey information
    private Long surveyId;

    // Creation and update timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
