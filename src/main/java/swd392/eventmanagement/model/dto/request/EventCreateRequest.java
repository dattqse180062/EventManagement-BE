package swd392.eventmanagement.model.dto.request;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;
import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.TargetAudience;

@Data
public class EventCreateRequest {
    private String name;
    private String description;
    private Long typeId;
    private TargetAudience audience;
    private String posterUrl;
    private String bannerUrl;
    private EventMode mode;

    // Location
    private LocationCreateRequest location;

    // Capacity
    private Integer maxCapacity;
    private Set<RoleCapacityCreateRequest> roleCapacities;

    // Platform
    private PlatformCreateRequest platform;

    // Tags
    private Set<Long> tags;

    // Images
    private Set<String> imageUrls;

    // Time
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
}
