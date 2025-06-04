package swd392.eventmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a staff member is not found in an event.
 * This exception is used when attempting to access, update, or remove staff
 * that are not assigned to the specified event.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EventStaffNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new EventStaffNotFoundException with the specified detail
     * message.
     * 
     * @param message the detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method)
     */
    public EventStaffNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new EventStaffNotFoundException with the specified detail
     * message and cause.
     * 
     * @param message the detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method)
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method)
     */
    public EventStaffNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new EventStaffNotFoundException with the specified cause.
     * 
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method)
     */
    public EventStaffNotFoundException(Throwable cause) {
        super(cause);
    }
}
