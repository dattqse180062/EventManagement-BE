package swd392.eventmanagement.exception;

/**
 * Exception thrown when processing event type operations fails
 */
public class EventTypeProcessingException extends RuntimeException {

    public EventTypeProcessingException(String message) {
        super(message);
    }

    public EventTypeProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
