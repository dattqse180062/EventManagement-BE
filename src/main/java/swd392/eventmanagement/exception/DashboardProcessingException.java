package swd392.eventmanagement.exception;

/**
 * Exception thrown when processing dashboard operations fails
 */
public class DashboardProcessingException extends RuntimeException {

    public DashboardProcessingException(String message) {
        super(message);
    }

    public DashboardProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}