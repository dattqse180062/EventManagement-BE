package swd392.eventmanagement.exception;

public class StaffRoleProcessingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public StaffRoleProcessingException(String message) {
        super(message);
    }

    public StaffRoleProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
