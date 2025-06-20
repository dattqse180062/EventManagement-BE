package swd392.eventmanagement.service.survey;

import org.springframework.http.ResponseEntity;
import swd392.eventmanagement.model.dto.request.SurveyCreateRequest;
import swd392.eventmanagement.model.dto.request.SurveySubmissionRequest;
import swd392.eventmanagement.model.dto.request.SurveyUpdateRequest;
import swd392.eventmanagement.model.dto.response.SurveyResponse;
import swd392.eventmanagement.model.dto.response.SurveyResponseDetail;
import swd392.eventmanagement.model.dto.response.SurveyUserResponse;

import java.util.List;

public interface SurveyService {
    SurveyResponse createSurveyWithQuestions(SurveyCreateRequest request, String departmentCode);

    SurveyResponse updateSurveyWithQuestions(Long surveyId, SurveyUpdateRequest request, String departmentCode);

    public SurveyResponse viewSurveyDetailByEventIdAndOpenStatus(Long eventId);

    void removeSurvey(Long surveyId, Long eventId, String departmentCode);

    public SurveyResponse viewSurveyDetailByEventId(Long eventId);

    void submitSurveyAnswerBySurveyId(SurveySubmissionRequest request);

    SurveyUserResponse getUserSurveyResponseByResponseId(Long responseId);

    void updateSurveyResponseByResponseId(Long responseId, SurveySubmissionRequest request);

    public List<SurveyResponseDetail> getSurveyResponsesByDepartmentAndSurvey(Long surveyId, String departmentCode);

    ResponseEntity<byte[]> exportSurveyResponsesToCSV(Long surveyId, String departmentCode);
}
