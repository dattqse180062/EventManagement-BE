package swd392.eventmanagement.exception;

/**
 * Exception thrown when processing department role operations fails
 */
public class DepartmentRoleProcessingException extends RuntimeException {

    public DepartmentRoleProcessingException(String message) {
        super(message);
    }

    public DepartmentRoleProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
