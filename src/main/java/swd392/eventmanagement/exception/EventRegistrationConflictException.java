package swd392.eventmanagement.exception;

public class EventRegistrationConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EventRegistrationConflictException(String message) {
        super(message);
    }

    public EventRegistrationConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}