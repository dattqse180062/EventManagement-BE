package swd392.eventmanagement.exception;

/**
 * Exception thrown when processing department operations fails
 */
public class DepartmentProcessingException extends RuntimeException {

    public DepartmentProcessingException(String message) {
        super(message);
    }

    public DepartmentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
