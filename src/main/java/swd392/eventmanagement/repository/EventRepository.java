package swd392.eventmanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.enums.TargetAudience;
import swd392.eventmanagement.model.entity.Department;
import swd392.eventmanagement.model.entity.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
        @Query("SELECT e FROM Event e JOIN Registration r ON e.id = r.event.id WHERE r.user.id = :userId")
        List<Event> findEventsByUserId(@Param("userId") Long userId);

        Optional<Event> findById(Long id);

        List<Event> findByDepartment(Department department);

        List<Event> findByStatus(EventStatus status);

        List<Event> findByStatusIn(Set<EventStatus> statuses);

        List<Event> findByStartTimeAfterAndStatus(LocalDateTime dateTime, EventStatus status);

        List<Event> findByDepartmentAndStatus(Department department, EventStatus status);

        @Query("SELECT e FROM Event e WHERE e.name LIKE %:keyword% OR e.description LIKE %:keyword%")
        Page<Event> searchByKeyword(String keyword, Pageable pageable);

        List<Event> findByAudienceAndStatus(TargetAudience audience, EventStatus status);

        List<Event> findByStartTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, EventStatus status);

        List<Event> findAllByStatusAndRegistrationEndBefore(EventStatus status, LocalDateTime dateTime);

        List<Event> findAllByStatusAndEndTimeBefore(EventStatus status, LocalDateTime dateTime);

        @Query(value = "SELECT COUNT(*) FROM events", nativeQuery = true)
        long countAllEvents();

        @Query(value = "SELECT COUNT(*) FROM events WHERE status = 'PUBLISHED'", nativeQuery = true)
        long countActiveEvents();

        @Query(value = "SELECT COUNT(*) FROM events WHERE start_time > NOW()", nativeQuery = true)
        long countUpcomingEvents();

        // Event registrations over time (by month)
        @Query(value = "SELECT EXTRACT(MONTH FROM start_time) AS month,COUNT(*) AS count "
                        + "FROM events WHERE EXTRACT (YEAR FROM start_time) =:year GROUP BY month ORDER BY month", nativeQuery = true)
        List<Object[]> countEventsByMonth(int year);

        // Event types distribution
        @Query(value = "SELECT et.name, COUNT(*) FROM events e JOIN event_types et ON e.type_id = et.id " +
                        "WHERE EXTRACT(YEAR FROM e.start_time) = :year GROUP BY et.name", nativeQuery = true)
        List<Object[]> countEventTypesByYear(int year);

        @Query("SELECT e FROM Event e " +
                        "JOIN e.eventCategories ec " +
                        "JOIN ec.category c " +
                        "WHERE c.code = :categoryCode " +
                        "ORDER BY ec.priority DESC")
        List<Event> findEventsByCategoryCodeOrderByPriority(
                        @Param("categoryCode") String categoryCode);
}
