package swd392.eventmanagement.exception;

/**
 * Exception thrown when processing tag operations fails
 */
public class TagProcessingException extends RuntimeException {

    public TagProcessingException(String message) {
        super(message);
    }

    public TagProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
