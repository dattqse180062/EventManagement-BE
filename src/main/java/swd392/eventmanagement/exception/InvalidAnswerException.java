package swd392.eventmanagement.exception;

public class InvalidAnswerException extends RuntimeException {
    public InvalidAnswerException(String message) {
        super(message);
    }

    public InvalidAnswerException(String message, Throwable cause) {
        super(message, cause);
    }
}
