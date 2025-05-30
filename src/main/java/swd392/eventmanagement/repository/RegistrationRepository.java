package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import swd392.eventmanagement.enums.RegistrationStatus;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.Registration;
import swd392.eventmanagement.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUser(User user);

    List<Registration> findByEvent(Event event);

    List<Registration> findByStatus(RegistrationStatus status);

    Optional<Registration> findByUserAndEvent(User user, Event event);

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event = ?1")
    Long countByEvent(Event event);

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event = ?1 AND r.status = ?2")
    Long countByEventAndStatus(Event event, RegistrationStatus status);

    List<Registration> findByEventAndStatus(Event event, RegistrationStatus status);

    List<Registration> findByUserAndStatus(User user, RegistrationStatus status);

    @Query("SELECT r FROM Registration r WHERE r.user = ?1 AND r.event.startTime > CURRENT_TIMESTAMP")
    List<Registration> findUpcomingEventsByUser(User user);

    @Query("SELECT COUNT(r) FROM Registration r JOIN r.user.roles role WHERE r.event = ?1 AND role.name = ?2 AND r.status != 'CANCELED'")
    Long countByEventAndUserRole(Event event, String roleName);

    @Query("SELECT r FROM Registration r WHERE r.user.id = ?1 AND r.event.id = ?2")
    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);
}
