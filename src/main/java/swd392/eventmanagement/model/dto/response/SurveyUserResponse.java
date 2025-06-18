package swd392.eventmanagement.model.dto.response;

import lombok.Data;
import swd392.eventmanagement.model.dto.request.QuestionAnswerRequest;

import java.util.List;

@Data
public class SurveyUserResponse {
    private Long surveyId;
    private List<QuestionAnswerRequest> answers;
}
