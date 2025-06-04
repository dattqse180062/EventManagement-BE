package swd392.eventmanagement.service.event.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import swd392.eventmanagement.exception.AccessDeniedException;
import swd392.eventmanagement.exception.DepartmentNotFoundException;
import swd392.eventmanagement.exception.DuplicateResourceException;
import swd392.eventmanagement.exception.EventNotFoundException;
import swd392.eventmanagement.exception.EventProcessingException;
import swd392.eventmanagement.exception.EventStaffNotFoundException;
import swd392.eventmanagement.exception.StaffRoleNotFoundException;
import swd392.eventmanagement.exception.StaffRoleProcessingException;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.model.dto.request.StaffManageRequest;
import swd392.eventmanagement.model.dto.response.StaffResponse;
import swd392.eventmanagement.model.dto.response.StaffRoleResponse;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventStaff;
import swd392.eventmanagement.model.entity.StaffRole;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.StaffRoleRepository;
import swd392.eventmanagement.model.mapper.EventStaffMapper;
import swd392.eventmanagement.model.mapper.StaffRoleMapper;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.EventStaffRepository;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.service.event.StaffService;
import swd392.eventmanagement.service.event.builder.EventStaffBuilder;
import swd392.eventmanagement.service.event.validator.EventManageAccessValidator;
import swd392.eventmanagement.service.event.validator.EventStaffCreateValidator;

@Service
@Transactional
public class StaffServiceImpl implements StaffService {
    private static final Logger logger = LoggerFactory.getLogger(StaffServiceImpl.class);
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventManageAccessValidator manageAccessValidator;
    private final EventStaffBuilder eventStaffBuilder;
    private final EventStaffMapper eventStaffMapper;
    private final EventStaffCreateValidator eventStaffCreateValidator;
    private final EventStaffRepository eventStaffRepository;
    private final StaffRoleRepository staffRoleRepository;
    private final StaffRoleMapper staffRoleMapper;

    public StaffServiceImpl(
            EventRepository eventRepository,
            UserRepository userRepository,
            EventManageAccessValidator manageAccessValidator,
            EventStaffBuilder eventStaffBuilder,
            EventStaffMapper eventStaffMapper,
            EventStaffCreateValidator eventStaffCreateValidator,
            EventStaffRepository eventStaffRepository, StaffRoleRepository staffRoleRepository,
            StaffRoleMapper staffRoleMapper) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.manageAccessValidator = manageAccessValidator;
        this.eventStaffBuilder = eventStaffBuilder;
        this.eventStaffMapper = eventStaffMapper;
        this.eventStaffCreateValidator = eventStaffCreateValidator;
        this.eventStaffRepository = eventStaffRepository;
        this.staffRoleRepository = staffRoleRepository;
        this.staffRoleMapper = staffRoleMapper;
    }

    @Override
    public List<StaffRoleResponse> getAllStaffRoles() {
        logger.info("Fetching all available staff roles");

        try {
            // Get all staff roles from repository
            List<StaffRole> staffRoles = staffRoleRepository.findAll();

            if (staffRoles.isEmpty()) {
                logger.warn("No staff roles found in the database");
                throw new StaffRoleNotFoundException("No staff roles found");
            }

            // Use the mapper to convert entities to DTOs
            List<StaffRoleResponse> responses = staffRoleMapper.toStaffRoleResponseList(staffRoles);

            logger.info("Successfully retrieved {} staff roles", responses.size());
            return responses;
        } catch (StaffRoleNotFoundException e) {
            // Rethrow StaffRoleNotFoundException directly for 404 response
            logger.error("Staff role not found", e);
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions in StaffRoleProcessingException for 500 response
            logger.error("Error processing staff roles", e);
            throw new StaffRoleProcessingException("Failed to process staff roles", e);
        }
    }

    @Override
    public List<StaffResponse> getEventStaffs(Long eventId, String departmentCode) {
        logger.info("Getting all staff for event ID: {} in department: {}", eventId, departmentCode);

        try {
            // Validate user has permission to view staff in this department
            Department department = manageAccessValidator.validateUserDepartmentAccess(departmentCode);

            // Get and validate event
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

            // Validate event belongs to the correct department
            manageAccessValidator.validateEventDepartmentAccess(event, department, departmentCode);

            // Get all staff assigned to the event and map to response
            List<EventStaff> eventStaffs = eventStaffRepository.findByEvent(event);
            List<StaffResponse> responses = eventStaffMapper.processEventStaffList(eventStaffs);

            logger.info("Found {} staff members in event ID: {}", responses.size(), eventId);
            return responses;

        } catch (EventNotFoundException | AccessDeniedException | DepartmentNotFoundException e) {
            // Rethrow specific exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error getting staff for event with ID: {}", eventId, e);
            throw new EventProcessingException("Failed to get staff for event", e);
        }
    }

    @Override
    @Transactional
    public StaffResponse assignStaff(Long eventId, String departmentCode, StaffManageRequest staffCreateRequest) {
        logger.info("Assigning staff with email: {} to event ID: {} with roles: {} and department: {}",
                staffCreateRequest.getEmail(), eventId, staffCreateRequest.getRoleName(), departmentCode);

        try {
            // Validate user has permission to assign staff in this department first
            Department department = manageAccessValidator.validateUserDepartmentAccess(departmentCode);

            // Get and validate event
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

            // Validate event belongs to the correct department
            manageAccessValidator.validateEventDepartmentAccess(event, department, departmentCode);

            // Get and validate user
            User user = userRepository.findByEmail(staffCreateRequest.getEmail())
                    .orElseThrow(() -> new UserNotFoundException(
                            "User not found with email: " + staffCreateRequest.getEmail()));
            eventStaffCreateValidator.validateEventStaffNotExist(event, user);

            // Create and save the staff assignments using builder pattern
            Set<EventStaff> eventStaffSet = eventStaffBuilder.createEventStaffWithRoles(
                    event, user, staffCreateRequest.getRoleName());

            if (eventStaffSet.isEmpty()) {
                throw new StaffRoleNotFoundException("No valid roles provided for staff assignment");
            }
            EventStaff firstEventStaff = eventStaffSet.iterator().next();
            StaffResponse response = eventStaffMapper.toStaffResponse(firstEventStaff);

            // Get roles from all created EventStaff records
            Set<String> assignedRoles = eventStaffSet.stream()
                    .map(eventStaff -> eventStaff.getStaffRole().getStaffRoleName())
                    .collect(Collectors.toSet());
            response.setRoleName(assignedRoles);

            // Use timestamps from the first EventStaff since they're all the same
            response.setAssignedAt(firstEventStaff.getAssignedAt());
            response.setUpdatedAt(firstEventStaff.getUpdatedAt());

            logger.info("Successfully assigned staff to event - Staff ID: {}, Event ID: {}, Roles: {}",
                    user.getId(), eventId, staffCreateRequest.getRoleName());
            return response;
        } catch (EventNotFoundException | UserNotFoundException | AccessDeniedException
                | DepartmentNotFoundException | StaffRoleNotFoundException | DuplicateResourceException e) {
            // Rethrow specific exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error assigning staff to event with ID: {}", eventId, e);
            throw new EventProcessingException("Failed to assign staff to event", e);
        }
    }

    @Override
    @Transactional
    public StaffResponse updateStaff(Long eventId, String departmentCode, StaffManageRequest staffUpdateRequest) {
        logger.info("Updating staff with email: {} in event ID: {} with roles: {} and department: {}",
                staffUpdateRequest.getEmail(), eventId, staffUpdateRequest.getRoleName(), departmentCode);

        try {
            // Validate user has permission to update staff in this department
            Department department = manageAccessValidator.validateUserDepartmentAccess(departmentCode);

            // Get and validate event
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

            // Validate event belongs to the correct department
            manageAccessValidator.validateEventDepartmentAccess(event, department, departmentCode); // Get and validate
                                                                                                    // user
            User user = userRepository.findByEmail(staffUpdateRequest.getEmail())
                    .orElseThrow(() -> new UserNotFoundException(
                            "User not found with email: " + staffUpdateRequest.getEmail()));

            // Validate that staff is assigned to event
            eventStaffCreateValidator.validateEventStaffExists(event, user, staffUpdateRequest.getEmail());

            // Create and save the updated staff assignments using builder pattern
            Set<EventStaff> eventStaffSet = eventStaffBuilder.updateEventStaffWithRoles(
                    event, user, staffUpdateRequest.getRoleName());

            if (eventStaffSet.isEmpty()) {
                throw new StaffRoleNotFoundException("No valid roles provided for staff update");
            }

            // Map response using the first EventStaff record
            EventStaff firstEventStaff = eventStaffSet.iterator().next();
            StaffResponse response = eventStaffMapper.toStaffResponse(firstEventStaff);

            // Get roles from all updated EventStaff records
            Set<String> assignedRoles = eventStaffSet.stream()
                    .map(eventStaff -> eventStaff.getStaffRole().getStaffRoleName())
                    .collect(Collectors.toSet());
            response.setRoleName(assignedRoles);

            // Use the earliest assignedAt among all eventStaff
            eventStaffSet.stream()
                    .map(EventStaff::getAssignedAt)
                    .min(LocalDateTime::compareTo)
                    .ifPresent(response::setAssignedAt);

            // Optionally: use the latest updatedAt among all eventStaff
            eventStaffSet.stream()
                    .map(EventStaff::getUpdatedAt)
                    .max(LocalDateTime::compareTo)
                    .ifPresent(response::setUpdatedAt);

            logger.info("Successfully updated staff in event - Staff ID: {}, Event ID: {}, Roles: {}",
                    user.getId(), eventId, staffUpdateRequest.getRoleName());
            return response;

        } catch (EventNotFoundException | UserNotFoundException | AccessDeniedException
                | DepartmentNotFoundException | StaffRoleNotFoundException e) {
            // Rethrow specific exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error updating staff in event with ID: {}", eventId, e);
            throw new EventProcessingException("Failed to update staff in event", e);
        }
    }

    @Override
    public void removeStaff(Long eventId, String departmentCode, String staffEmail) {
        logger.info("Removing staff with email: {} from event ID: {} and department: {}",
                staffEmail, eventId, departmentCode);

        try {
            // Validate user has permission to remove staff from this department
            Department department = manageAccessValidator.validateUserDepartmentAccess(departmentCode);

            // Get and validate event
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

            // Validate event belongs to the correct department
            manageAccessValidator.validateEventDepartmentAccess(event, department, departmentCode);

            User user = userRepository.findByEmail(staffEmail)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + staffEmail));

            // Validate that staff is assigned to event
            eventStaffCreateValidator.validateEventStaffExists(event, user, staffEmail);

            // Remove staff from event
            eventStaffRepository.deleteByEventAndStaff(event, user);

            logger.info("Successfully removed staff from event - Staff ID: {}, Event ID: {}",
                    user.getId(), eventId);
        } catch (EventNotFoundException | UserNotFoundException | AccessDeniedException
                | DepartmentNotFoundException | EventStaffNotFoundException e) {
            // Rethrow specific exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error removing staff from event with ID: {}", eventId, e);
            throw new EventProcessingException("Failed to remove staff from event", e);
        }
    }
}
