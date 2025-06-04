package swd392.eventmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UserProcessingException extends RuntimeException {

    public UserProcessingException(String message) {
        super(message);
    }

    public UserProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
