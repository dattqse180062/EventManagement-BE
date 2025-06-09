package swd392.eventmanagement.service.survey;

import swd392.eventmanagement.model.dto.request.SurveyCreateRequest;
import swd392.eventmanagement.model.dto.request.SurveyUpdateRequest;
import swd392.eventmanagement.model.dto.response.SurveyResponse;

public interface SurveyService {
    SurveyResponse createSurveyWithQuestions(SurveyCreateRequest request, String departmentCode);

    SurveyResponse updateSurveyWithQuestions(Long surveyId, SurveyUpdateRequest request, String departmentCode);
}
