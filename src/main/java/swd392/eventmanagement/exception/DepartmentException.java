package swd392.eventmanagement.exception;

public class DepartmentException extends RuntimeException {
  public DepartmentException(String message) {
    super(message);
  }

  public DepartmentException(String message, Throwable cause) {
    super(message, cause);
  }
}
