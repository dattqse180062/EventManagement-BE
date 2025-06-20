package swd392.eventmanagement.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SurveyResponseDetail {
    private Long responseId;
    private String userName;
    private LocalDateTime submittedAt;
    private Map<String, String> answers; // Map<QuestionText, AnswerValue>
}
