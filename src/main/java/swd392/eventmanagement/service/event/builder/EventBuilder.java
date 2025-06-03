package swd392.eventmanagement.service.event.builder;

import java.time.LocalDateTime;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.exception.EventRequestValidationException;
import swd392.eventmanagement.exception.EventTypeNotFoundException;
import swd392.eventmanagement.model.dto.request.EventCreateRequest;
import swd392.eventmanagement.model.dto.request.EventUpdateRequest;
import swd392.eventmanagement.model.dto.request.LocationCreateRequest;
import swd392.eventmanagement.model.dto.request.PlatformCreateRequest;
import swd392.eventmanagement.model.dto.request.RoleCapacityCreateRequest;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventType;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.EventTypeRepository;
import swd392.eventmanagement.repository.RegistrationRepository;

@Component
public class EventBuilder {
    private static final Logger logger = LoggerFactory.getLogger(EventBuilder.class);

    private final EventTypeRepository eventTypeRepository;
    private final EventRepository eventRepository;
    private final LocationBuilder locationBuilder;
    private final PlatformBuilder platformBuilder;
    private final TagBuilder tagBuilder;
    private final ImageBuilder imageBuilder;
    private final EventCapacityBuilder capacityBuilder;

    public EventBuilder(
            EventTypeRepository eventTypeRepository,
            EventRepository eventRepository,
            LocationBuilder locationBuilder,
            PlatformBuilder platformBuilder,
            TagBuilder tagBuilder,
            ImageBuilder imageBuilder,
            EventCapacityBuilder capacityBuilder) {
        this.eventTypeRepository = eventTypeRepository;
        this.eventRepository = eventRepository;
        this.locationBuilder = locationBuilder;
        this.platformBuilder = platformBuilder;
        this.tagBuilder = tagBuilder;
        this.imageBuilder = imageBuilder;
        this.capacityBuilder = capacityBuilder;
    }

    /**
     * Creates a new event entity with basic fields from the create request
     * 
     * @param request    The event creation request
     * @param department The department the event belongs to
     * @return A new event entity with basic fields set
     * @throws EventTypeNotFoundException If the specified event type doesn't exist
     */
    public Event createEventWithBasicFields(EventCreateRequest request, Department department) {
        logger.debug("Creating new event with name: {} for department: {}", request.getName(), department.getCode());

        // Validate event type
        EventType eventType = eventTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> {
                    logger.error("Event type not found with id: {}", request.getTypeId());
                    return new EventTypeNotFoundException("Event type not found with id: " + request.getTypeId());
                });

        // Create event entity
        Event event = new Event();
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setDepartment(department);
        event.setType(eventType);
        event.setAudience(request.getAudience());
        event.setPosterUrl(request.getPosterUrl());
        event.setBannerUrl(request.getBannerUrl());
        event.setMode(request.getMode());
        event.setMaxCapacity(request.getMaxCapacity());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setRegistrationStart(request.getRegistrationStart());
        event.setRegistrationEnd(request.getRegistrationEnd());
        event.setStatus(EventStatus.DRAFT);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        logger.info("Created event with basic fields - name: {}, type: {}, mode: {}, audience: {}",
                event.getName(), eventType.getName(), event.getMode(), event.getAudience());
        return event;
    }

    /**
     * Updates basic properties of an event from the update request
     * Following PUT semantics, all fields are replaced with values from the request
     * 
     * @param event   Event entity to update
     * @param request Update request containing new values
     * @throws EventTypeNotFoundException If the specified event type doesn't exist
     */
    public void updateEventBasicFields(Event event, EventUpdateRequest request,
            RegistrationRepository registrationRepository) {
        logger.debug("Updating basic fields for event ID: {}", event.getId());

        // Required fields (validated in validateEventUpdateRequest)
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setPosterUrl(request.getPosterUrl());
        event.setBannerUrl(request.getBannerUrl());
        event.setMode(request.getMode());
        event.setAudience(request.getAudience());

        // Event type
        EventType eventType = eventTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> {
                    logger.error("Event type not found with id: {}", request.getTypeId());
                    return new EventTypeNotFoundException("Event type not found with id: " + request.getTypeId());
                });

        // Check max capacity
        if (request.getMaxCapacity() != null) {
            Long currentRegistrationCount = registrationRepository.countByEvent(event);
            if (currentRegistrationCount != null && request.getMaxCapacity() < currentRegistrationCount) {
                logger.warn(
                        "Attempted to reduce capacity below current registrations - Event: {}, Current registrations: {}, Requested capacity: {}",
                        event.getId(), currentRegistrationCount, request.getMaxCapacity());
                throw new EventRequestValidationException(
                        "Cannot reduce maximum capacity below the current number of registrations ("
                                + currentRegistrationCount + ")");
            }
            event.setMaxCapacity(request.getMaxCapacity());
        }

        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setRegistrationStart(request.getRegistrationStart());
        event.setRegistrationEnd(request.getRegistrationEnd());

        // Always update the updatedAt timestamp
        event.setUpdatedAt(LocalDateTime.now());

        logger.info("Updated event basic fields - ID: {}, name: {}, type: {}, mode: {}, audience: {}",
                event.getId(), event.getName(), eventType.getName(), event.getMode(), event.getAudience());
    }

    /**
     * Applies event updates including related entities
     * Use for both create and update operations
     * 
     * @param event           The event entity to update
     * @param tags            Set of tag IDs to associate with the event
     * @param imageUrls       Set of image URLs to associate with the event
     * @param locationRequest Location data to associate with the event
     * @param platformRequest Platform data to associate with the event
     * @param roleCapacities  Set of role capacities to associate with the event
     * @return The updated event entity
     */
    public Event applyEventUpdates(
            Event event,
            Set<Long> tags,
            Set<String> imageUrls,
            LocationCreateRequest locationRequest,
            PlatformCreateRequest platformRequest,
            Set<RoleCapacityCreateRequest> roleCapacities) {

        logger.debug("Applying updates to event ID: {} - mode: {}", event.getId(), event.getMode());

        // Handle mode-specific requirements
        if (event.getMode() == EventMode.OFFLINE) {
            logger.debug("OFFLINE mode - removing platform data for event ID: {}", event.getId());
            platformRequest = null;
        } else if (event.getMode() == EventMode.ONLINE) {
            logger.debug("ONLINE mode - removing location data for event ID: {}", event.getId());
            locationRequest = null;
        }

        try {
            // Handle location
            locationBuilder.updateEventLocation(event, locationRequest);

            // Handle platform
            platformBuilder.updateEventPlatform(event, platformRequest);

            // Save event to ensure it has an ID for relationships
            event = eventRepository.save(event);
            logger.debug("Saved event core data - ID: {}", event.getId());

            // Handle related entities
            tagBuilder.updateEventTags(event, tags);
            imageBuilder.updateEventImages(event, imageUrls);
            capacityBuilder.updateEventCapacities(event, roleCapacities);

            logger.info("Successfully applied all updates to event ID: {}", event.getId());
            return event;
        } catch (Exception e) {
            logger.error("Error while applying updates to event ID: {} - Error: {}", event.getId(), e.getMessage());
            throw e;
        }
    }
}
