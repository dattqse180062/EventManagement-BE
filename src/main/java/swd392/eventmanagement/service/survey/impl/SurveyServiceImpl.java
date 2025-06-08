package swd392.eventmanagement.service.survey.impl;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.enums.SurveyStatus;
import swd392.eventmanagement.exception.SurveyProcessingException;
import swd392.eventmanagement.model.dto.request.QuestionCreateRequest;
import swd392.eventmanagement.model.dto.request.SurveyCreateRequest;
import swd392.eventmanagement.model.dto.response.OptionResponse;
import swd392.eventmanagement.model.dto.response.QuestionResponse;
import swd392.eventmanagement.model.dto.response.SurveyResponse;
import swd392.eventmanagement.model.entity.Option;
import swd392.eventmanagement.model.entity.Question;
import swd392.eventmanagement.model.entity.Survey;
import swd392.eventmanagement.repository.QuestionRepository;
import swd392.eventmanagement.repository.SurveyRepository;
import swd392.eventmanagement.service.event.validator.EventManageAccessValidator;
import swd392.eventmanagement.service.survey.SurveyService;
import swd392.eventmanagement.service.survey.validator.SurveyManageAccessValidator;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {
    private static final Logger logger = LoggerFactory.getLogger(SurveyServiceImpl.class);
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final SurveyManageAccessValidator surveyManageAccessValidator;


    @Override
    public SurveyResponse createSurveyWithQuestions(SurveyCreateRequest request,String departmentCode) {
        logger.info("Creating new survey with title: {}", request.getTitle());

        //. Validate user's permission for the given department code
        surveyManageAccessValidator.validateUserDepartmentAccess(departmentCode);

        try {
            // Step 0: Check if survey already exists (by title, start and end time)
            boolean exists = surveyRepository.existsByTitleAndStartTimeAndEndTime(
                    request.getTitle(), request.getStartTime(), request.getEndTime());

            if (exists) {
                throw new SurveyProcessingException("Survey with the same title and time range already exists");
            }

            // Step 1: Create and save survey
            Survey survey = new Survey();
            survey.setTitle(request.getTitle());
            survey.setDescription(request.getDescription());
            survey.setStartTime(request.getStartTime());
            survey.setEndTime(request.getEndTime());
            survey.setStatus(SurveyStatus.DRAFT);

            Survey savedSurvey = surveyRepository.save(survey);

            // Step 2: Create associated questions
            List<QuestionResponse> questionResponses = new ArrayList<>();

            for (QuestionCreateRequest qReq : request.getQuestions()) {
                Question question = new Question();
                question.setSurvey(savedSurvey);
                question.setQuestion(qReq.getQuestion());
                question.setOrderNum(qReq.getOrderNum());
                question.setType(qReq.getType());
                question.setIsRequired(qReq.getIsRequired());

                // Handle options if provided
                if (qReq.getOptions() != null && !qReq.getOptions().isEmpty()) {
                    List<Option> options = qReq.getOptions().stream().map(optReq -> {
                        Option option = new Option();
                        option.setText(optReq.getText());
                        option.setOrderNum(optReq.getOrderNum());
                        option.setQuestion(question); // Link option to question
                        return option;
                    }).collect(Collectors.toList());

                    question.setOptions(options);
                }
                Question savedQuestion = questionRepository.save(question);

                // Convert saved question to response DTO
                QuestionResponse qResp = new QuestionResponse();
                qResp.setId(savedQuestion.getId());
                qResp.setQuestion(savedQuestion.getQuestion());
                qResp.setOrderNum(savedQuestion.getOrderNum());
                qResp.setType(savedQuestion.getType());
                qResp.setIsRequired(savedQuestion.getIsRequired());

                List<OptionResponse> optionResponses = savedQuestion.getOptions() != null
                        ? savedQuestion.getOptions().stream().map(opt -> {
                    OptionResponse oResp = new OptionResponse();
                    oResp.setId(opt.getId());
                    oResp.setText(opt.getText());
                    oResp.setOrderNum(opt.getOrderNum());
                    return oResp;
                }).collect(Collectors.toList())
                        : new ArrayList<>();

                qResp.setOptions(optionResponses);
                questionResponses.add(qResp);
            }

            // Step 3: Build survey response object
            SurveyResponse surveyResponse = new SurveyResponse();
            surveyResponse.setId(savedSurvey.getId());
            surveyResponse.setTitle(savedSurvey.getTitle());
            surveyResponse.setDescription(savedSurvey.getDescription());
            surveyResponse.setStartTime(savedSurvey.getStartTime());
            surveyResponse.setEndTime(savedSurvey.getEndTime());
            surveyResponse.setStatus(savedSurvey.getStatus());
            surveyResponse.setCreatedAt(savedSurvey.getCreatedAt());
            surveyResponse.setUpdatedAt(savedSurvey.getUpdatedAt());
            surveyResponse.setQuestions(questionResponses);

            return surveyResponse;

        } catch (Exception e) {
            logger.error("Failed to create survey and questions", e);
            throw new SurveyProcessingException("Failed to create survey and questions: " + e.getMessage(), e);
        }
    }
}
