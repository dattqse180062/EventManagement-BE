package swd392.eventmanagement.service;

import swd392.eventmanagement.model.dto.request.QuestionCreateRequest;
import swd392.eventmanagement.model.dto.response.QuestionResponse;

import java.util.List;

public interface SurveyService {
    List<QuestionResponse> createQuestionForSurvey(Long surveyId, List<QuestionCreateRequest> requestList);
}
