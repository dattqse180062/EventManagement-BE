package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.EventCapacity;
import swd392.eventmanagement.model.entity.Role;

import java.util.List;
import java.util.Optional;

public interface EventCapacityRepository extends JpaRepository<EventCapacity, Long> {
    List<EventCapacity> findByEvent(Event event);

    List<EventCapacity> findByRole(Role role);

    Optional<EventCapacity> findByEventAndRole(Event event, Role role);
}
