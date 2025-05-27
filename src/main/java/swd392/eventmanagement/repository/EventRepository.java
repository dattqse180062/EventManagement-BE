package swd392.eventmanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.enums.TargetAudience;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event e JOIN Registration r ON e.id = r.event.id WHERE r.user.id = :userId")
    List<Event> findEventsByUserId(@Param("userId") Long userId);

    List<Event> findByDepartment(Department department);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByStartTimeAfterAndStatus(LocalDateTime dateTime, EventStatus status);

    List<Event> findByDepartmentAndStatus(Department department, EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.name LIKE %:keyword% OR e.description LIKE %:keyword%")
    Page<Event> searchByKeyword(String keyword, Pageable pageable);

    List<Event> findByAudienceAndStatus(TargetAudience audience, EventStatus status);

    List<Event> findByStartTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, EventStatus status);
}
