package swd392.eventmanagement.service.event.impl;

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
import swd392.eventmanagement.exception.StaffRoleNotFoundException;
import swd392.eventmanagement.exception.UserNotFoundException;
import swd392.eventmanagement.model.dto.request.StaffCreateRequest;
import swd392.eventmanagement.model.dto.response.StaffResponse;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventStaff;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.model.mapper.EventStaffMapper;
import swd392.eventmanagement.repository.EventRepository;
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

    public StaffServiceImpl(
            EventRepository eventRepository,
            UserRepository userRepository,
            EventManageAccessValidator manageAccessValidator,
            EventStaffBuilder eventStaffBuilder,
            EventStaffMapper eventStaffMapper,
            EventStaffCreateValidator eventStaffCreateValidator) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.manageAccessValidator = manageAccessValidator;
        this.eventStaffBuilder = eventStaffBuilder;
        this.eventStaffMapper = eventStaffMapper;
        this.eventStaffCreateValidator = eventStaffCreateValidator;
    }

    @Override
    @Transactional
    public StaffResponse assignStaff(Long eventId, String departmentCode, StaffCreateRequest staffCreateRequest) {
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
}
