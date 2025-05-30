package swd392.eventmanagement.service.event.status;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.exception.InvalidStateTransitionException;

@Service
public class EventStatusStateMachine {
    private final Map<EventStatus, Set<EventStatus>> transitions;

    public EventStatusStateMachine(Map<EventStatus, Set<EventStatus>> transitions) {
        this.transitions = transitions;
    }

    public void validateTransition(EventStatus current, EventStatus target) {
        Set<EventStatus> allowed = transitions.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new InvalidStateTransitionException(current, target, allowed);
        }
    }
}
