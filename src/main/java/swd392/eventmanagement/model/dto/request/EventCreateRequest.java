package swd392.eventmanagement.model.dto.request;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Data;
import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.TargetAudience;

@Data
public class EventCreateRequest {
    @NotBlank(message = "Event name cannot be null or empty")
    @Size(max = 255, message = "Event name cannot exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Event type ID cannot be null")
    private Long typeId;

    @NotNull(message = "Target audience cannot be null")
    private TargetAudience audience;

    private String posterUrl;
    private String bannerUrl;

    @NotNull(message = "Event mode cannot be null")
    private EventMode mode;

    // Location
    @Valid
    private LocationCreateRequest location;

    // Capacity
    @Positive(message = "Max capacity must be greater than 0")
    private Integer maxCapacity;

    @Valid
    private Set<RoleCapacityCreateRequest> roleCapacities;

    // Platform
    @Valid
    private PlatformCreateRequest platform;

    // Tags
    private Set<Long> tags;

    // Images
    private Set<String> imageUrls;

    // Time
    @NotNull(message = "Start time cannot be null")
    private LocalDateTime startTime;

    @NotNull(message = "End time cannot be null")
    private LocalDateTime endTime;

    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
}
