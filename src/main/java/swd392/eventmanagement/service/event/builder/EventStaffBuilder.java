package swd392.eventmanagement.service.event.builder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import swd392.eventmanagement.exception.StaffRoleNotFoundException;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventStaff;
import swd392.eventmanagement.model.entity.StaffRole;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.repository.EventStaffRepository;
import swd392.eventmanagement.repository.StaffRoleRepository;

@Component
public class EventStaffBuilder {
    private final EventStaffRepository eventStaffRepository;
    private final StaffRoleRepository staffRoleRepository;

    public EventStaffBuilder(EventStaffRepository eventStaffRepository, StaffRoleRepository staffRoleRepository) {
        this.eventStaffRepository = eventStaffRepository;
        this.staffRoleRepository = staffRoleRepository;
    }

    /**
     * Updates the staff roles for an event
     * Using PUT semantics - clears existing roles and adds new ones if provided
     * 
     * @param event     Event entity to update
     * @param staff     User entity to be assigned as staff
     * @param roleNames Set of role names to associate with the staff
     * @return Set of created EventStaff entities
     * @throws StaffRoleNotFoundException If a role name doesn't exist or has
     *                                    invalid prefix
     */
    public Set<EventStaff> createEventStaffWithRoles(Event event, User staff, Set<String> roleNames) {
        Set<EventStaff> eventStaffSet = new HashSet<>();

        if (roleNames != null && !roleNames.isEmpty()) {
            for (String roleName : roleNames) {
                // Add EVENT_ prefix if it's missing
                String eventRoleName = roleName.startsWith("EVENT_") ? roleName : "EVENT_" + roleName;

                StaffRole staffRole = staffRoleRepository.findByStaffRoleName(eventRoleName)
                        .orElseThrow(() -> new StaffRoleNotFoundException(
                                "Staff role not found with name: " + eventRoleName));

                eventStaffSet.add(createEventStaffRecord(event, staff, staffRole, LocalDateTime.now()));
            }
        }

        return eventStaffSet;
    }

    /**
     * Updates the staff roles for an existing staff member in an event
     * First stores original assignedAt times, then removes old roles and adds new
     * ones
     * 
     * @param event     Event entity to update
     * @param staff     User entity whose roles need to be updated
     * @param roleNames Set of new role names to assign
     * @return Set of updated EventStaff entities
     * @throws StaffRoleNotFoundException If a role name doesn't exist or has
     *                                    invalid prefix
     */
    public Set<EventStaff> updateEventStaffWithRoles(Event event, User staff, Set<String> roleNames) {
        // Store original assignedAt times
        List<EventStaff> existingEventStaffList = eventStaffRepository.findByEventAndStaff(event, staff);

        // Determine the earliest assignedAt time
        LocalDateTime earliestAssignedAt = existingEventStaffList.stream()
                .map(EventStaff::getAssignedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        // Delete old assignments
        if (!existingEventStaffList.isEmpty()) {
            eventStaffRepository.deleteAll(existingEventStaffList);
            eventStaffRepository.flush();
        }

        // Create new staff roles using the earliest assignedAt
        Set<EventStaff> eventStaffSet = new HashSet<>();
        if (roleNames != null && !roleNames.isEmpty()) {
            for (String roleName : roleNames) {
                String eventRoleName = roleName.startsWith("EVENT_") ? roleName : "EVENT_" + roleName;
                StaffRole staffRole = staffRoleRepository.findByStaffRoleName(eventRoleName)
                        .orElseThrow(() -> new StaffRoleNotFoundException(
                                "Staff role not found with name: " + eventRoleName));

                eventStaffSet.add(createEventStaffRecord(event, staff, staffRole, earliestAssignedAt));
            }
        }

        return eventStaffSet;
    }

    /**
     * Creates a single EventStaff record with the given parameters
     * 
     * @param event      Event entity to assign staff to
     * @param staff      User entity to be assigned as staff
     * @param role       StaffRole entity for the assignment
     * @param assignedAt The timestamp when staff was first assigned to this role
     * @return The saved EventStaff entity
     */
    private EventStaff createEventStaffRecord(Event event, User staff, StaffRole role, LocalDateTime assignedAt) {
        EventStaff eventStaff = new EventStaff();
        eventStaff.setEvent(event);
        eventStaff.setStaff(staff);
        eventStaff.setStaffRole(role);
        eventStaff.setAssignedAt(assignedAt);
        eventStaff.setUpdatedAt(LocalDateTime.now());

        return eventStaffRepository.save(eventStaff);
    }
}
