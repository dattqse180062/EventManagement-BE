package swd392.eventmanagement.exception;

import java.util.Set;
import swd392.eventmanagement.enums.EventStatus;

public class InvalidStateTransitionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidStateTransitionException(EventStatus currentStatus, EventStatus targetStatus,
            Set<EventStatus> allowedTransitions) {
        super(String.format("Invalid transition: %s -> %s. Allowed: %s", currentStatus, targetStatus,
                allowedTransitions));
    }
}
