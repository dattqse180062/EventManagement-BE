package swd392.eventmanagement.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import swd392.eventmanagement.security.service.UserDetailsImpl;

import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.exception.AccessDeniedException;
import swd392.eventmanagement.exception.DepartmentNotFoundException;
import swd392.eventmanagement.exception.EventNotFoundException;
import swd392.eventmanagement.exception.EventProcessingException;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.model.dto.response.EventDetailsDTO;
import swd392.eventmanagement.model.dto.response.EventListDTO;
import swd392.eventmanagement.model.dto.response.EventListManagementDTO;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.DepartmentRole;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventCapacity;
import swd392.eventmanagement.model.mapper.EventMapper;
import swd392.eventmanagement.repository.DepartmentRepository;
import swd392.eventmanagement.repository.EventCapacityRepository;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.EventSpecification;
import swd392.eventmanagement.repository.RegistrationRepository;
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

    public EventServiceImpl(
            EventRepository eventRepository,
            EventMapper eventMapper,
            RegistrationRepository registrationRepository,
            EventCapacityRepository eventCapacityRepository,
            DepartmentRepository departmentRepository,
            UserDepartmentRoleRepository userDepartmentRoleRepository,
            DepartmentRoleRepository departmentRoleRepository,
            UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.registrationRepository = registrationRepository;
        this.eventCapacityRepository = eventCapacityRepository;
        this.departmentRepository = departmentRepository;
        this.userDepartmentRoleRepository = userDepartmentRoleRepository;
        this.departmentRoleRepository = departmentRoleRepository;
        this.userRepository = userRepository;
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
