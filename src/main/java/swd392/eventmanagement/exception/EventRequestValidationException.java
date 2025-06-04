package swd392.eventmanagement.exception;

public class EventRequestValidationException extends RuntimeException {
    public EventRequestValidationException(String message) {
        super(message);
    }

    public EventRequestValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
