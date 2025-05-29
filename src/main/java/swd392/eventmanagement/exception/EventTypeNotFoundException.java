package swd392.eventmanagement.exception;

public class EventTypeNotFoundException extends RuntimeException {

    public EventTypeNotFoundException(String message) {
        super(message);
    }

    public EventTypeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
