package swd392.eventmanagement.model.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SurveySubmissionRequest {
  private Long surveyId;
   private Long registrationId; // optional
    private List<QuestionAnswerRequest> answers;
}
