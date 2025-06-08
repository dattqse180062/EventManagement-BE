package swd392.eventmanagement.exception;

public class SurveyProcessingException extends RuntimeException {

    public SurveyProcessingException(String message) {
        super(message);
    }

    public SurveyProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
