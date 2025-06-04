package swd392.eventmanagement.exception;

public class EventRegistrationException extends RuntimeException {
    public EventRegistrationException(String message) {
        super(message);
    }

    public EventRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
