package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventStaff;
import swd392.eventmanagement.model.entity.StaffRole;
import swd392.eventmanagement.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface EventStaffRepository extends JpaRepository<EventStaff, Long> {
    List<EventStaff> findByEvent(Event event);

    List<EventStaff> findByStaff(User staff);

    List<EventStaff> findByStaffRole(StaffRole staffRole);

    Optional<EventStaff> findByEventAndStaffAndStaffRole(Event event, User staff, StaffRole staffRole);

    boolean existsByEventAndStaffAndStaffRole(Event event, User staff, StaffRole staffRole);
}
