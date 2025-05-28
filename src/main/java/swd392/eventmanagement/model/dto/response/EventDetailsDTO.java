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

/**
 * Data Transfer Object for detailed information about an event.
 * This class is used to return comprehensive information about a specific
 * event.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailsDTO {
    // Basic event information
    private Long id;
    private String name;
    private String description;
    private EventStatus status; // Department information
    private String departmentAvatarUrl;
    private String departmentName;

    // Event type information
    private String typeName;

    // Audience and capacity
    private TargetAudience audience;
    private Integer maxCapacity;
    private Integer registeredCount;
    private Integer maxCapacityStudent;
    private Integer registeredCountStudent;
    private Integer maxCapacityLecturer;
    private Integer registeredCountLecturer;

    // Location information
    private String locationAddress;
    private String locationAddress2;

    // Event timing
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;

    // Media content
    private String posterUrl;
    private String bannerUrl; // Event mode and platform
    private EventMode mode;
    private String platformName;

    // Tags associated with the event
    @Builder.Default
    private Set<TagShowDTO> tags = new HashSet<>(); // Event images
    @Builder.Default
    private Set<ImageDTO> images = new HashSet<>();

    // Creation and update timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional data for the current user
    private Boolean isRegistered; // Whether the current user is registered for this event
    private String registrationStatus; // Status of the user's registration if registered
}
