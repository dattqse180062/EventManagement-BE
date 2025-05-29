package swd392.eventmanagement.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
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

import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.exception.AccessDeniedException;
import swd392.eventmanagement.exception.DepartmentNotFoundException;
import swd392.eventmanagement.exception.EventNotFoundException;
import swd392.eventmanagement.exception.EventProcessingException;
import swd392.eventmanagement.exception.EventRequestValidationException;
import swd392.eventmanagement.exception.EventTypeNotFoundException;
import swd392.eventmanagement.exception.TagNotFoundException;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.model.dto.request.EventCreateRequest;
import swd392.eventmanagement.model.dto.request.LocationCreateRequest;
import swd392.eventmanagement.model.dto.request.PlatformCreateRequest;
import swd392.eventmanagement.model.dto.request.RoleCapacityCreateRequest;
import swd392.eventmanagement.model.dto.response.EventDetailsDTO;
import swd392.eventmanagement.model.dto.response.EventDetailsManagementDTO;
import swd392.eventmanagement.model.dto.response.EventListDTO;
import swd392.eventmanagement.model.dto.response.EventListManagementDTO;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.DepartmentRole;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventCapacity;
import swd392.eventmanagement.model.entity.EventType;
import swd392.eventmanagement.model.entity.Image;
import swd392.eventmanagement.model.entity.Location;
import swd392.eventmanagement.model.entity.Platform;
import swd392.eventmanagement.model.entity.Role;
import swd392.eventmanagement.model.entity.Tag;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.model.mapper.EventMapper;
import swd392.eventmanagement.repository.DepartmentRepository;
import swd392.eventmanagement.repository.EventCapacityRepository;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.EventSpecification;
import swd392.eventmanagement.repository.EventTypeRepository;
import swd392.eventmanagement.repository.ImageRepository;
import swd392.eventmanagement.repository.LocationRepository;
import swd392.eventmanagement.repository.PlatformRepository;
import swd392.eventmanagement.repository.RegistrationRepository;
import swd392.eventmanagement.repository.RoleRepository;
import swd392.eventmanagement.repository.TagRepository;
import swd392.eventmanagement.repository.UserDepartmentRoleRepository;
import swd392.eventmanagement.repository.DepartmentRoleRepository;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.service.EventService;

@Service
public class EventServiceImpl implements EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RegistrationRepository registrationRepository;
    private final EventCapacityRepository eventCapacityRepository;
    private final DepartmentRepository departmentRepository;
    private final UserDepartmentRoleRepository userDepartmentRoleRepository;
    private final DepartmentRoleRepository departmentRoleRepository;
    private final UserRepository userRepository;
    private final EventTypeRepository eventTypeRepository;
    private final TagRepository tagRepository;
    private final LocationRepository locationRepository;
    private final PlatformRepository platformRepository;
    private final ImageRepository imageRepository;
    private final RoleRepository roleRepository;

    public EventServiceImpl(
            EventRepository eventRepository,
            EventMapper eventMapper,
            RegistrationRepository registrationRepository,
            EventCapacityRepository eventCapacityRepository,
            DepartmentRepository departmentRepository,
            UserDepartmentRoleRepository userDepartmentRoleRepository,
            DepartmentRoleRepository departmentRoleRepository,
            UserRepository userRepository,
            EventTypeRepository eventTypeRepository,
            TagRepository tagRepository,
            LocationRepository locationRepository,
            PlatformRepository platformRepository,
            ImageRepository imageRepository,
            RoleRepository roleRepository) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.registrationRepository = registrationRepository;
        this.eventCapacityRepository = eventCapacityRepository;
        this.departmentRepository = departmentRepository;
        this.userDepartmentRoleRepository = userDepartmentRoleRepository;
        this.departmentRoleRepository = departmentRoleRepository;
        this.userRepository = userRepository;
        this.eventTypeRepository = eventTypeRepository;
        this.tagRepository = tagRepository;
        this.locationRepository = locationRepository;
        this.platformRepository = platformRepository;
        this.imageRepository = imageRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<EventListManagementDTO> getEventsForManagement(String departmentCode) {
        logger.info("Getting events for management for department code: {}", departmentCode);
        try {
            // Find the department by code
            Department department = departmentRepository.findByCode(departmentCode)
                    .orElseThrow(
                            () -> new DepartmentNotFoundException("No department found with code: " + departmentCode));

            // Check if the current user is the HEAD of the department
            if (!isHeadOfDepartment(department)) {
                logger.warn("Unauthorized access attempt to department events by non-HEAD user for department: {}",
                        departmentCode);
                throw new AccessDeniedException(
                        "Access denied. Only department HEAD can access management events for department: "
                                + departmentCode);
            }

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
    public List<EventListDTO> getAvailableEvents() {
        try {
            List<Event> events = eventRepository.findByStatus(EventStatus.PUBLISHED);

            if (events.isEmpty()) {
                logger.info("No published events found");
                throw new EventNotFoundException("No published events are currently available");
            }

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

            // Get current authenticated user (if any)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;
            if (auth != null && auth.isAuthenticated() && !("anonymousUser".equals(auth.getPrincipal()))) {
                userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            } // Use MapStruct to map the base entity properties
            EventDetailsDTO eventDetailsDTO = eventMapper.toEventDetailsDTO(event); // Get total registration count
            Long registeredCount = registrationRepository.countByEvent(event);
            eventDetailsDTO.setRegisteredCount(registeredCount != null ? registeredCount.intValue() : 0);

            // Student and Lecturer capacities and counts
            for (EventCapacity capacity : eventCapacityRepository.findByEvent(event)) {
                String roleName = capacity.getRole().getName();
                if ("ROLE_STUDENT".equals(roleName)) {
                    eventDetailsDTO.setMaxCapacityStudent(capacity.getCapacity());
                    // Count student registrations
                    Long studentCount = registrationRepository.countByEventAndUserRole(event, "ROLE_STUDENT");
                    eventDetailsDTO.setRegisteredCountStudent(studentCount != null ? studentCount.intValue() : 0);
                } else if ("ROLE_LECTURER".equals(roleName)) {
                    eventDetailsDTO.setMaxCapacityLecturer(capacity.getCapacity());
                    // Count lecturer registrations
                    Long lecturerCount = registrationRepository.countByEventAndUserRole(event, "ROLE_LECTURER");
                    eventDetailsDTO.setRegisteredCountLecturer(lecturerCount != null ? lecturerCount.intValue() : 0);
                }
            }

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
    public EventDetailsManagementDTO getEventDetailsForManagement(String departmentCode, Long eventId) {
        logger.info("Getting event details for management for department code: {} and event ID: {}", departmentCode,
                eventId);
        try {
            if (eventId == null) {
                throw new IllegalArgumentException("Event ID cannot be null");
            }

            if (departmentCode == null || departmentCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Department code cannot be null or empty");
            }

            // Find the department by code
            Department department = departmentRepository.findByCode(departmentCode)
                    .orElseThrow(
                            () -> new DepartmentNotFoundException("No department found with code: " + departmentCode));

            // Fetch event by ID
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
            if (!event.getDepartment().getId().equals(department.getId())) {
                logger.warn("Event with ID: {} does not belong to department with code: {}", eventId, departmentCode);
                throw new AccessDeniedException(
                        "Access denied. Event with ID: " + eventId + " does not belong to department: "
                                + departmentCode);
            }

            // Check if the current user is authorized to view this event's management
            // details
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                logger.warn("Unauthorized access attempt to event management details for event ID: {}", eventId);
                throw new AccessDeniedException("Access denied. Authentication required.");
            }

            // Verify user is the HEAD of the department
            if (!isHeadOfDepartment(department)) {
                logger.warn(
                        "Unauthorized access attempt to event management details by non-HEAD user for department: {} and event ID: {}",
                        departmentCode, eventId);
                throw new AccessDeniedException("Access denied. Only department HEAD can access management details.");
            }

            // Map the event entity to EventDetailsManagement DTO
            EventDetailsManagementDTO eventDetailsManagement = eventMapper.toEventDetailsManagement(event);
            setEventCapacityInfo(eventDetailsManagement, event);

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
    public List<EventListDTO> searchEvents(
            String name,
            List<Long> tagIds,
            Long typeId,
            EventStatus status,
            LocalDateTime from,
            LocalDateTime to,
            EventMode mode,
            Long departmentId) {
        try {
            Specification<Event> spec = EventSpecification.filter(
                    name,
                    tagIds,
                    typeId,
                    status,
                    from,
                    to, mode,
                    departmentId != null && departmentId > 0 ? departmentId : null);
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
    @Transactional
    public EventDetailsManagementDTO createNewEvent(EventCreateRequest eventCreateRequest, String departmentCode) {
        logger.info("Creating new event with name: {}", eventCreateRequest.getName());

        try {
            // Input validation
            validateEventCreateRequest(eventCreateRequest); // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                throw new AccessDeniedException("Access denied. Authentication required.");
            } // Find and validate department
            Department department = departmentRepository.findByCode(departmentCode)
                    .orElseThrow(() -> new DepartmentNotFoundException(
                            "No department found with code: " + departmentCode));

            // Check if user is HEAD of the department
            if (!isHeadOfDepartment(department)) {
                throw new AccessDeniedException(
                        "Access denied. Only department HEAD can create events for department: "
                                + departmentCode);
            }
            // Validate event type
            EventType eventType = eventTypeRepository.findById(eventCreateRequest.getTypeId())
                    .orElseThrow(() -> new EventTypeNotFoundException(
                            "Event type not found with id: " + eventCreateRequest.getTypeId()));

            // Create event entity
            Event event = new Event();
            event.setName(eventCreateRequest.getName());
            event.setDescription(eventCreateRequest.getDescription());
            event.setDepartment(department);
            event.setType(eventType);
            event.setAudience(eventCreateRequest.getAudience());
            event.setPosterUrl(eventCreateRequest.getPosterUrl());
            event.setBannerUrl(eventCreateRequest.getBannerUrl());
            event.setMode(eventCreateRequest.getMode());
            event.setMaxCapacity(eventCreateRequest.getMaxCapacity());
            event.setStartTime(eventCreateRequest.getStartTime());
            event.setEndTime(eventCreateRequest.getEndTime());
            event.setRegistrationStart(eventCreateRequest.getRegistrationStart());
            event.setRegistrationEnd(eventCreateRequest.getRegistrationEnd());
            event.setStatus(EventStatus.DRAFT); // New events start as DRAFT
            event.setCreatedAt(LocalDateTime.now());
            event.setUpdatedAt(LocalDateTime.now());

            // Handle location creation if provided
            if (eventCreateRequest.getLocation() != null) {
                Location location = createLocation(eventCreateRequest.getLocation());
                event.setLocation(location);
            }

            // Handle platform creation if provided
            if (eventCreateRequest.getPlatform() != null) {
                Platform platform = createPlatform(eventCreateRequest.getPlatform());
                event.setPlatform(platform);
            } // Handle tags if provided
            if (eventCreateRequest.getTags() != null && !eventCreateRequest.getTags().isEmpty()) {
                Set<Tag> tags = new HashSet<>();
                for (Long tagId : eventCreateRequest.getTags()) {
                    Tag tag = tagRepository.findById(tagId)
                            .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + tagId));
                    tags.add(tag);
                }
                event.setTags(tags);
            }

            // Handle images if provided
            if (eventCreateRequest.getImageUrls() != null && !eventCreateRequest.getImageUrls().isEmpty()) {
                Set<Image> images = new HashSet<>();
                for (String imageUrl : eventCreateRequest.getImageUrls()) {
                    Image image = new Image();
                    image.setUrl(imageUrl);
                    image.setEvent(event);
                    images.add(imageRepository.save(image));
                }
                event.setImages(images);
            }

            // Save event first to get ID for capacities
            Event savedEvent = eventRepository.save(event);
            // Create event capacities for roles if provided
            if (eventCreateRequest.getRoleCapacities() != null && !eventCreateRequest.getRoleCapacities().isEmpty()) {
                createEventCapacities(savedEvent, new HashSet<>(eventCreateRequest.getRoleCapacities()));
            }

            // Map to DTO and return
            EventDetailsManagementDTO result = eventMapper.toEventDetailsManagement(savedEvent);

            // Set capacity information
            setEventCapacityInfo(result, savedEvent);

            logger.info("Successfully created event with ID: {} and name: {}", savedEvent.getId(),
                    savedEvent.getName());
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

    /**
     * Validates the event create request
     */
    private void validateEventCreateRequest(EventCreateRequest request) {
        if (request == null) {
            throw new EventRequestValidationException("Event create request cannot be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new EventRequestValidationException("Event name cannot be null or empty");
        }

        if (request.getTypeId() == null) {
            throw new EventRequestValidationException("Event type ID cannot be null");
        }
        // Check if the event type exists in the database
        if (!eventTypeRepository.existsById(request.getTypeId())) {
            throw new EventTypeNotFoundException("Event type not found with id: " + request.getTypeId());
        }

        if (request.getStartTime() == null) {
            throw new EventRequestValidationException("Start time cannot be null");
        }

        if (request.getEndTime() == null) {
            throw new EventRequestValidationException("End time cannot be null");
        }

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new EventRequestValidationException("Start time cannot be after end time");
        }

        // Check if start time is in the past
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new EventRequestValidationException("Start time cannot be in the past");
        }

        if (request.getRegistrationStart() != null && request.getRegistrationEnd() != null) {
            if (request.getRegistrationStart().isAfter(request.getRegistrationEnd())) {
                throw new EventRequestValidationException(
                        "Registration start time cannot be after registration end time");
            }
            if (request.getRegistrationEnd().isAfter(request.getStartTime())) {
                throw new EventRequestValidationException("Registration end time must be before event start time");
            }
        }

        if (request.getMaxCapacity() != null && request.getMaxCapacity() <= 0) {
            throw new EventRequestValidationException("Max capacity must be greater than 0");
        } // Validate role capacities if provided
        if (request.getRoleCapacities() != null && !request.getRoleCapacities().isEmpty()) {
            // Validate that role names are not empty
            for (RoleCapacityCreateRequest roleCapacity : request.getRoleCapacities()) {
                if (roleCapacity.getRoleName() == null || roleCapacity.getRoleName().trim().isEmpty()) {
                    throw new EventRequestValidationException("Role name cannot be empty in capacity settings");
                }
                if (roleCapacity.getMaxCapacity() == null || roleCapacity.getMaxCapacity() <= 0) {
                    throw new EventRequestValidationException(
                            "Max capacity for role " + roleCapacity.getRoleName() + " must be greater than 0");
                }

                // Normalize role name to check if it exists in database
                String roleName = roleCapacity.getRoleName();
                String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

                // Check if the role exists in the database (optional validation)
                // This could be moved to service implementation if not desired during
                // validation
                if (!roleRepository.findByName(normalizedRoleName).isPresent()) {
                    logger.warn("Role with name {} does not exist in the database", normalizedRoleName);
                }
            }

            int totalCapacity = request.getRoleCapacities().stream()
                    .mapToInt(rc -> rc.getMaxCapacity())
                    .sum();

            if (request.getMaxCapacity() != null && totalCapacity > request.getMaxCapacity()) {
                throw new EventRequestValidationException("Sum of role capacities cannot exceed max capacity");
            }
        }
    }

    /**
     * Creates a location from location create request
     */
    private Location createLocation(LocationCreateRequest locationRequest) {
        Location location = new Location();
        location.setAddress(locationRequest.getAddress());
        location.setWard(locationRequest.getWard());
        location.setDistrict(locationRequest.getDistrict());
        location.setCity(locationRequest.getCity());
        return locationRepository.save(location);
    }

    /**
     * Creates a platform from platform create request
     */
    private Platform createPlatform(PlatformCreateRequest platformRequest) {
        Platform platform = new Platform();
        platform.setName(platformRequest.getName());
        platform.setUrl(platformRequest.getUrl());
        return platformRepository.save(platform);
    }

    /**
     * Creates event capacities for different roles
     */
    private void createEventCapacities(Event event,
            Set<RoleCapacityCreateRequest> roleCapacities) {
        for (RoleCapacityCreateRequest roleCapacityRequest : roleCapacities) {
            // Prefix role name with "ROLE_" if not already prefixed
            String roleName = roleCapacityRequest.getRoleName();
            String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            Role role = roleRepository.findByName(normalizedRoleName)
                    .orElseThrow(
                            () -> new EventRequestValidationException(
                                    "Role not found with name: " + normalizedRoleName));

            EventCapacity eventCapacity = new EventCapacity();
            // Initialize the composite ID
            EventCapacity.EventCapacityId id = new EventCapacity.EventCapacityId();
            id.setEventId(event.getId());
            id.setRoleId(role.getId());
            eventCapacity.setId(id);

            eventCapacity.setEvent(event);
            eventCapacity.setRole(role);
            eventCapacity.setCapacity(roleCapacityRequest.getMaxCapacity());

            eventCapacityRepository.save(eventCapacity);
        }
    }

    /**
     * Sets capacity information for the event DTO
     */
    private void setEventCapacityInfo(EventDetailsManagementDTO dto, Event event) {
        for (EventCapacity capacity : eventCapacityRepository.findByEvent(event)) {
            String roleName = capacity.getRole().getName();
            // Check both with and without the ROLE_ prefix
            if ("ROLE_STUDENT".equals(roleName)) {
                dto.setMaxCapacityStudent(capacity.getCapacity());
            } else if ("ROLE_LECTURER".equals(roleName)) {
                dto.setMaxCapacityLecturer(capacity.getCapacity());
            }
        }
    }

    /**
     * Checks if the current authenticated user is the HEAD of the specified
     * department.
     * 
     * @param department The department to check
     * @return true if the user is the HEAD of the department, false otherwise
     */
    private boolean isHeadOfDepartment(Department department) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("No authenticated user found when checking for department HEAD role");
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        try {
            // Get the user entity
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

            // Find the HEAD role
            DepartmentRole headRole = departmentRoleRepository.findByName("HEAD")
                    .orElseThrow(() -> new RuntimeException("Department role 'HEAD' not found"));

            // Check if the user has the HEAD role in the specific department
            return userDepartmentRoleRepository.findByUserAndDepartmentAndDepartmentRole(user, department, headRole)
                    .isPresent();

        } catch (Exception e) {
            logger.error("Error checking if user is HEAD of department: {}", department.getCode(), e);
            return false;
        }
    }
}
