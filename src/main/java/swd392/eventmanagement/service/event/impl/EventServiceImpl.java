package swd392.eventmanagement.service.event.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import swd392.eventmanagement.security.service.UserDetailsImpl;
import swd392.eventmanagement.service.event.EventService;
import swd392.eventmanagement.service.event.builder.EventBuilder;
import swd392.eventmanagement.service.event.builder.EventCapacityBuilder;
import swd392.eventmanagement.service.event.builder.ImageBuilder;
import swd392.eventmanagement.service.event.builder.LocationBuilder;
import swd392.eventmanagement.service.event.builder.PlatformBuilder;
import swd392.eventmanagement.service.event.builder.TagBuilder;
import swd392.eventmanagement.service.event.validator.EventCreateValidator;
import swd392.eventmanagement.service.event.validator.EventManageAccessValidator;
import swd392.eventmanagement.service.event.validator.EventUpdateValidator;
import swd392.eventmanagement.service.event.status.EventStatusStateMachine;
import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.enums.TargetAudience;
import swd392.eventmanagement.exception.AccessDeniedException;
import swd392.eventmanagement.exception.DepartmentNotFoundException;
import swd392.eventmanagement.exception.EventNotFoundException;
import swd392.eventmanagement.exception.EventProcessingException;
import swd392.eventmanagement.exception.EventRequestValidationException;
import swd392.eventmanagement.exception.EventTypeNotFoundException;
import swd392.eventmanagement.exception.InvalidStateTransitionException;
import swd392.eventmanagement.exception.TagNotFoundException;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.model.dto.request.EventCreateRequest;
import swd392.eventmanagement.model.dto.request.EventUpdateRequest;
import swd392.eventmanagement.model.dto.response.EventDetailsDTO;
import swd392.eventmanagement.model.dto.response.EventDetailsManagementDTO;
import swd392.eventmanagement.model.dto.response.EventListDTO;
import swd392.eventmanagement.model.dto.response.EventListManagementDTO;
import swd392.eventmanagement.model.dto.response.EventUpdateStatusResponse;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventCapacity;
import swd392.eventmanagement.model.mapper.EventMapper;
import swd392.eventmanagement.repository.EventCapacityRepository;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.RegistrationRepository;
import swd392.eventmanagement.repository.spec.EventSpecification;

@Service
public class EventServiceImpl implements EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RegistrationRepository registrationRepository;
    private final EventCapacityRepository eventCapacityRepository;
    private final EventBuilder eventBuilder;
    private final EventCapacityBuilder capacityBuilder;
    private final EventCreateValidator createValidator;
    private final EventUpdateValidator updateValidator;
    private final EventManageAccessValidator manageAccessValidator;
    private final EventStatusStateMachine eventStatusStateMachine;

    public EventServiceImpl(
            EventRepository eventRepository,
            EventMapper eventMapper,
            RegistrationRepository registrationRepository,
            EventCapacityRepository eventCapacityRepository,
            EventBuilder eventBuilder,
            EventCapacityBuilder capacityBuilder,
            EventCreateValidator createValidator,
            EventUpdateValidator updateValidator,
            LocationBuilder locationBuilder,
            PlatformBuilder platformBuilder,
            TagBuilder tagBuilder,
            ImageBuilder imageBuilder,
            EventManageAccessValidator manageValidator,
            EventStatusStateMachine eventStatusStateMachine) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.registrationRepository = registrationRepository;
        this.eventCapacityRepository = eventCapacityRepository;
        this.eventBuilder = eventBuilder;
        this.capacityBuilder = capacityBuilder;
        this.createValidator = createValidator;
        this.updateValidator = updateValidator;
        this.manageAccessValidator = manageValidator;
        this.eventStatusStateMachine = eventStatusStateMachine;
    }

    @Override
    public List<EventListDTO> getAvailableEvents() {
        try {
            List<Event> events = eventRepository.findByStatusIn(Set.of(
                    EventStatus.PUBLISHED,
                    EventStatus.BLOCKED,
                    EventStatus.CLOSED,
                    EventStatus.COMPLETED));

            if (events.isEmpty()) {
                logger.info("No published events found");
                throw new EventNotFoundException("No published events are currently available");
            }

            // Sort events by status in specified order: PUBLISHED -> BLOCKED -> CLOSED ->
            // COMPLETED
            events.sort((e1, e2) -> {
                List<EventStatus> order = List.of(
                        EventStatus.PUBLISHED,
                        EventStatus.BLOCKED,
                        EventStatus.CLOSED,
                        EventStatus.COMPLETED);
                return Integer.compare(order.indexOf(e1.getStatus()), order.indexOf(e2.getStatus()));
            });

            logger.info("Found {} published events", events.size());
            return eventMapper.toDTOList(events);
        } catch (Exception e) {
            logger.error("Error retrieving available events", e);
            throw new EventProcessingException("Failed to retrieve available events", e);
        }
    }

    @Override
    public List<EventListDTO> getUserRegisteredEvents() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UserNotFoundException("User not authenticated");
        }

        Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();

        try {
            List<Event> events = eventRepository.findEventsByUserId(userId);

            if (events.isEmpty()) {
                logger.info("No registered events found for user {}", userId);
                throw new EventNotFoundException("No registered events found for the user");
            }

            logger.info("Found {} registered events for user {}", events.size(), userId);
            return eventMapper.toDTOList(events);

        } catch (Exception e) {
            logger.error("Error retrieving registered events for user {}", userId, e);
            throw new EventProcessingException("Failed to retrieve registered events", e);
        }
    }

    @Override
    public EventDetailsDTO getEventDetails(Long eventId) {
        logger.info("Getting event details for event ID: {}", eventId);
        try {
            if (eventId == null) {
                throw new IllegalArgumentException("Event ID cannot be null");
            }

            // Fetch event by ID
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

            if (event.getStatus() == EventStatus.DRAFT
                    || event.getStatus() == EventStatus.DELETED
                    || event.getStatus() == EventStatus.CANCELED) {
                throw new EventNotFoundException("Event not found with ID: " + eventId);
            }

            // Get current authenticated user (if any)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;
            if (auth != null && auth.isAuthenticated() && !("anonymousUser".equals(auth.getPrincipal()))) {
                userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            } // Use MapStruct to map the base entity properties
            EventDetailsDTO eventDetailsDTO = eventMapper.toEventDetailsDTO(event);

            // Student and Lecturer capacities and counts
            int totalRegisteredCount = 0;
            for (EventCapacity capacity : eventCapacityRepository.findByEvent(event)) {
                String roleName = capacity.getRole().getName();
                if ("ROLE_STUDENT".equals(roleName)) {
                    eventDetailsDTO.setMaxCapacityStudent(capacity.getCapacity());
                    // Count student registrations
                    Long studentCount = registrationRepository.countByEventAndUserRole(event, "ROLE_STUDENT");
                    int studentCountInt = studentCount != null ? studentCount.intValue() : 0;
                    eventDetailsDTO.setRegisteredCountStudent(studentCountInt);
                    totalRegisteredCount += studentCountInt;
                } else if ("ROLE_LECTURER".equals(roleName)) {
                    eventDetailsDTO.setMaxCapacityLecturer(capacity.getCapacity());
                    // Count lecturer registrations
                    Long lecturerCount = registrationRepository.countByEventAndUserRole(event, "ROLE_LECTURER");
                    int lecturerCountInt = lecturerCount != null ? lecturerCount.intValue() : 0;
                    eventDetailsDTO.setRegisteredCountLecturer(lecturerCountInt);
                    totalRegisteredCount += lecturerCountInt;
                }
            }
            eventDetailsDTO.setRegisteredCount(totalRegisteredCount);

            // Check if current user is registered for this event
            if (userId != null) {
                registrationRepository.findByUserIdAndEventId(userId, eventId).ifPresentOrElse(
                        registration -> {
                            eventDetailsDTO.setIsRegistered(true);
                            eventDetailsDTO.setRegistrationStatus(registration.getStatus().toString());
                        },
                        () -> {
                            eventDetailsDTO.setIsRegistered(false);
                            eventDetailsDTO.setRegistrationStatus(null);
                        });
            } else {
                eventDetailsDTO.setIsRegistered(false);
                eventDetailsDTO.setRegistrationStatus(null);
            }
            logger.info("Successfully retrieved event details for event ID: {}", eventId);
            return eventDetailsDTO;

        } catch (EventNotFoundException e) {
            // Just rethrow EventNotFoundException without additional logging
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving details for event with ID: {}", eventId, e);
            throw new EventProcessingException("Failed to retrieve event details", e);
        }
    }

    @Override
    public List<EventListDTO> searchEvents(
            String name,
            List<Long> tagIds,
            Long typeId,
            TargetAudience targetAudience,
            EventStatus status,
            LocalDateTime from,
            LocalDateTime to,
            EventMode mode,
            Long departmentId) {
        try {
            if (status == EventStatus.CANCELED || status == EventStatus.DRAFT || status == EventStatus.DELETED) {
                throw new EventProcessingException("Cannot search for events with DRAFT/DELETED/CANCELED status");
            }

            Specification<Event> spec = EventSpecification.filter(
                    name,
                    tagIds,
                    typeId,
                    targetAudience,
                    status,
                    from,
                    to,
                    mode,
                    departmentId);
            List<Event> events = eventRepository.findAll(spec);
            if (events.isEmpty()) {
                logger.info("No events found matching search criteria");
                throw new EventNotFoundException("No events found matching the search criteria");
            }

            logger.info("Found {} events matching search criteria", events.size());
            return eventMapper.toDTOList(events);
        } catch (EventNotFoundException e) {
            // Rethrow EventNotFoundException to be handled by the global exception handler
            // This ensures HTTP 404 response instead of HTTP 500
            throw e;
        } catch (Exception e) {
            logger.error("Error searching events", e);
            throw new EventProcessingException("Failed to search events", e);
        }
    }

    @Override
    public List<EventListManagementDTO> getEventsForManagement(String departmentCode) {
        logger.info("Getting events for management for department code: {}", departmentCode);
        try {
            // Validate user has permission to access events in this department
            Department department = manageAccessValidator.validateUserDepartmentAccess(departmentCode);

            // Get all events for this department
            List<Event> events = eventRepository.findByDepartment(department);

            if (events.isEmpty()) {
                logger.info("No events found for department with code: {}", departmentCode);
                throw new EventNotFoundException("No events found for department with code: " + departmentCode);
            }

            // Use EventMapper to map to DTOs
            List<EventListManagementDTO> eventDTOs = eventMapper.toEventListManagementDTOList(events);

            logger.info("Found {} events for department with code: {}", eventDTOs.size(), departmentCode);
            return eventDTOs;
        } catch (DepartmentNotFoundException | EventNotFoundException | AccessDeniedException e) {
            // Just rethrow exceptions without additional logging
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving events for department with code: {}", departmentCode, e);
            throw new EventProcessingException("Failed to retrieve events for management", e);
        }
    }

    @Override
    public EventDetailsManagementDTO getEventDetailsForManagement(String departmentCode, Long eventId) {
        logger.info("Getting event details for management for department code: {} and event ID: {}", departmentCode,
                eventId);
        try {
            if (eventId == null) {
                throw new IllegalArgumentException("Event ID cannot be null");
            } // Validate user has permission to access events in this department
            Department department = manageAccessValidator.validateUserDepartmentAccess(departmentCode);
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
            manageAccessValidator.validateEventDepartmentAccess(event, department, departmentCode);

            // Map the event entity to EventDetailsManagement DTO
            EventDetailsManagementDTO eventDetailsManagement = eventMapper.toEventDetailsManagement(event);
            capacityBuilder.setEventCapacityInfo(eventDetailsManagement, event);

            logger.info("Successfully retrieved event management details for department: {} and event ID: {}",
                    departmentCode, eventId);
            return eventDetailsManagement;
        } catch (EventNotFoundException | AccessDeniedException | DepartmentNotFoundException e) {
            // Just rethrow exceptions without additional logging
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving management details for department: {} and event ID: {}", departmentCode,
                    eventId, e);
            throw new EventProcessingException("Failed to retrieve event management details", e);
        }
    }

    @Override
    @Transactional
    public EventDetailsManagementDTO createNewEvent(EventCreateRequest eventCreateRequest, String departmentCode) {
        logger.info("Creating new event with name: {}", eventCreateRequest.getName());

        try { // Input validation
            createValidator.validateEventCreateRequest(eventCreateRequest);

            // Validate user has permission to create events in this department
            Department department = manageAccessValidator.validateUserDepartmentAccess(departmentCode);
            Event event = eventBuilder.createEventWithBasicFields(eventCreateRequest, department);

            // Save event first to get ID
            event = eventRepository.save(event);

            // Apply all updates using the common method
            event = eventBuilder.applyEventUpdates(
                    event,
                    eventCreateRequest.getTags(),
                    eventCreateRequest.getImageUrls(),
                    eventCreateRequest.getLocation(),
                    eventCreateRequest.getPlatform(),
                    eventCreateRequest.getRoleCapacities());

            // Map to DTO and return
            EventDetailsManagementDTO result = eventMapper.toEventDetailsManagement(event);

            // Set capacity information
            capacityBuilder.setEventCapacityInfo(result, event);

            logger.info("Successfully created event with ID: {} and name: {}", event.getId(), event.getName());
            return result;
        } catch (AccessDeniedException | DepartmentNotFoundException | EventTypeNotFoundException
                | UserNotFoundException | EventRequestValidationException | TagNotFoundException e) {
            // Rethrow specific exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error creating new event with name: {}", eventCreateRequest.getName(), e);
            throw new EventProcessingException("Failed to create new event", e);
        }
    }

    @Override
    @Transactional
    public EventDetailsManagementDTO updateEvent(Long eventId, EventUpdateRequest eventUpdateRequest,
            String departmentCode) {
        logger.info("Updating event with ID: {} in department: {}", eventId, departmentCode);

        try {
            if (eventId == null) {
                throw new IllegalArgumentException("Event ID cannot be null");
            } // Input validation - validates all fields required for PUT semantics
            updateValidator.validateEventUpdateRequest(eventUpdateRequest);

            // Validate user has permission to update events in this department
            Department department = manageAccessValidator.validateUserDepartmentAccess(departmentCode);
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

            if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.BLOCKED) {
                throw new EventProcessingException("Event can only be updated in DRAFT or BLOCKED status");
            }
            manageAccessValidator.validateEventDepartmentAccess(event, department, departmentCode);

            // Update basic fields first
            eventBuilder.updateEventBasicFields(event, eventUpdateRequest, registrationRepository);

            // Apply all other updates using the event builder
            event = eventBuilder.applyEventUpdates(
                    event,
                    eventUpdateRequest.getTags(),
                    eventUpdateRequest.getImageUrls(),
                    eventUpdateRequest.getLocation(),
                    eventUpdateRequest.getPlatform(),
                    eventUpdateRequest.getRoleCapacities());

            // Map to DTO and return
            EventDetailsManagementDTO result = eventMapper.toEventDetailsManagement(event);
            capacityBuilder.setEventCapacityInfo(result, event);

            logger.info("Successfully updated event with ID: {} and name: {}", event.getId(), event.getName());
            return result;
        } catch (AccessDeniedException | DepartmentNotFoundException | EventNotFoundException
                | EventTypeNotFoundException | UserNotFoundException | EventRequestValidationException
                | TagNotFoundException e) {
            // Rethrow specific exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error updating event with ID: {}", eventId, e);
            throw new EventProcessingException("Failed to update event", e);
        }
    }

    @Override
    @Transactional
    public EventUpdateStatusResponse updateEventStatus(Long eventId, EventStatus newStatus, String departmentCode) {
        logger.info("Updating event status - Event ID: {}, New Status: {}, Department: {}", eventId, newStatus,
                departmentCode);

        try {
            if (eventId == null) {
                throw new IllegalArgumentException("Event ID cannot be null");
            }
            if (newStatus == null) {
                throw new IllegalArgumentException("New status cannot be null");
            }

            // Validate user has permission to update events in this department
            Department department = manageAccessValidator.validateUserDepartmentAccess(departmentCode);

            // Get event and validate department access
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
            manageAccessValidator.validateEventDepartmentAccess(event, department, departmentCode);

            // Validate status transition
            EventStatus currentStatus = event.getStatus();
            eventStatusStateMachine.validateTransition(currentStatus, newStatus); // Update the status
            event.setStatus(newStatus);
            event.setUpdatedAt(LocalDateTime.now());
            event = eventRepository.save(event);

            // Map to response using mapper
            EventUpdateStatusResponse response = eventMapper.toEventUpdateStatusResponse(event, currentStatus);

            logger.info("Successfully updated event status - Event ID: {}, Previous Status: {}, New Status: {}",
                    eventId, currentStatus, newStatus);
            return response;

        } catch (EventNotFoundException | AccessDeniedException | DepartmentNotFoundException
                | InvalidStateTransitionException e) {
            // Just rethrow specific exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error updating status for event with ID: {}", eventId, e);
            throw new EventProcessingException("Failed to update event status", e);
        }
    }
}
