package swd392.eventmanagement.service.survey.impl;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.enums.SurveyStatus;
import swd392.eventmanagement.exception.SurveyNotFoundException;
import swd392.eventmanagement.exception.SurveyProcessingException;
import swd392.eventmanagement.model.dto.request.OptionRequest;
import swd392.eventmanagement.model.dto.request.QuestionRequest;
import swd392.eventmanagement.model.dto.request.SurveyCreateRequest;
import swd392.eventmanagement.model.dto.request.SurveyUpdateRequest;
import swd392.eventmanagement.model.dto.response.OptionResponse;
import swd392.eventmanagement.model.dto.response.QuestionResponse;
import swd392.eventmanagement.model.dto.response.SurveyResponse;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.Option;
import swd392.eventmanagement.model.entity.Question;
import swd392.eventmanagement.model.entity.Survey;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.OptionRepository;
import swd392.eventmanagement.repository.QuestionRepository;
import swd392.eventmanagement.repository.SurveyRepository;
import swd392.eventmanagement.service.survey.SurveyService;
import swd392.eventmanagement.service.survey.validator.SurveyManageAccessValidator;

import java.util.*;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {
    private static final Logger logger = LoggerFactory.getLogger(SurveyServiceImpl.class);
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final SurveyManageAccessValidator surveyManageAccessValidator;
    private final OptionRepository optionRepository;
    private final EventRepository eventRepository;


    @Override
    public SurveyResponse createSurveyWithQuestions(SurveyCreateRequest request, String departmentCode) {
        logger.info("Creating new survey with title: {}", request.getTitle());

        surveyManageAccessValidator.validateUserDepartmentAccess(departmentCode);

        try {
            // Check if the event exists
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new SurveyProcessingException("Event not found with id: " + request.getEventId()));

            // Check if the event already has a survey
            if (event.getSurvey() != null) {
                throw new SurveyProcessingException("This event already has a survey assigned");
            }

            // Check for duplicate survey title and time range
            boolean exists = surveyRepository.existsByTitleAndStartTimeAndEndTime(
                    request.getTitle(), request.getStartTime(), request.getEndTime());

            if (exists) {
                throw new SurveyProcessingException("Survey with the same title and time range already exists");
            }

            // Constraint 1: check for duplicate question content
            Set<String> uniqueQuestions = new HashSet<>();
            for (QuestionRequest qReq : request.getQuestions()) {
                String normalized = qReq.getQuestion().trim().toLowerCase();
                if (!uniqueQuestions.add(normalized)) {
                    throw new SurveyProcessingException("Duplicate question: " + normalized);
                }
            }

            // Constraint 2: check for duplicate order number
            Set<Integer> uniqueOrderNums = new HashSet<>();
            for (QuestionRequest qReq : request.getQuestions()) {
                if (!uniqueOrderNums.add(qReq.getOrderNum())) {
                    throw new SurveyProcessingException("Duplicate question order number: " + qReq.getOrderNum());
                }
            }

            // Create Survey entity and set its properties
            Survey survey = new Survey();
            survey.setTitle(request.getTitle());
            survey.setDescription(request.getDescription());
            survey.setStartTime(request.getStartTime());
            survey.setEndTime(request.getEndTime());
            survey.setStatus(SurveyStatus.DRAFT);

            Survey savedSurvey = surveyRepository.save(survey);

            // Set the survey to the event and save event
            event.setSurvey(savedSurvey);
            eventRepository.save(event);

            // Save questions and options
            List<QuestionResponse> questionResponses = new ArrayList<>();

            for (QuestionRequest qReq : request.getQuestions()) {
                Question question = new Question();
                question.setSurvey(savedSurvey);
                question.setQuestion(qReq.getQuestion());
                question.setOrderNum(qReq.getOrderNum());
                question.setType(qReq.getType());
                question.setIsRequired(qReq.getIsRequired());

                if (qReq.getOptions() != null && !qReq.getOptions().isEmpty()) {
                    List<Option> options = qReq.getOptions().stream().map(optReq -> {
                        Option option = new Option();
                        option.setText(optReq.getText());
                        option.setOrderNum(optReq.getOrderNum());
                        option.setQuestion(question);
                        return option;
                    }).collect(Collectors.toList());

                    question.setOptions(options);
                }

                Question savedQuestion = questionRepository.save(question);

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

    @Transactional
    @Override
    public SurveyResponse updateSurveyWithQuestions(Long surveyId, SurveyUpdateRequest request, String departmentCode) {
        logger.info("Updating survey id: {}", surveyId);

        surveyManageAccessValidator.validateUserDepartmentAccess(departmentCode);

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException("Survey with id " + surveyId + " not found"));

        // Check for duplicate questions (case-insensitive)
        Set<String> uniqueQuestions = new HashSet<>();
        for (QuestionRequest qReq : request.getQuestions()) {
            String normalizedQuestion = qReq.getQuestion().trim().toLowerCase();
            if (!uniqueQuestions.add(normalizedQuestion)) {
                throw new SurveyProcessingException("Duplicate question: " + normalizedQuestion);
            }
        }

        // Check for duplicate order numbers
        Set<Integer> uniqueOrderNums = new HashSet<>();
        for (QuestionRequest qReq : request.getQuestions()) {
            if (!uniqueOrderNums.add(qReq.getOrderNum())) {
                throw new SurveyProcessingException("Duplicate question order number: " + qReq.getOrderNum());
            }
        }

        // Update survey basic info
        survey.setTitle(request.getTitle());
        survey.setDescription(request.getDescription());
        survey.setStartTime(request.getStartTime());
        survey.setEndTime(request.getEndTime());

        // Save updated survey info
        survey = surveyRepository.save(survey);

        // Map existing questions by their ID
        Map<Long, Question> existingQuestionsMap = survey.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        List<Question> updatedQuestions = new ArrayList<>();

        for (QuestionRequest qReq : request.getQuestions()) {
            Question question;
            if (qReq.getId() != null) {
                question = existingQuestionsMap.remove(qReq.getId());
                if (question == null) {
                    throw new SurveyProcessingException("Question with id " + qReq.getId() + " not found");
                }
            } else {
                question = new Question();
                question.setSurvey(survey);
                question.setOptions(new ArrayList<>());
            }

            question.setQuestion(qReq.getQuestion());
            question.setOrderNum(qReq.getOrderNum());
            question.setType(qReq.getType());
            question.setIsRequired(qReq.getIsRequired());



            Map<Long, Option> existingOptionsMap = question.getOptions() != null
                    ? question.getOptions().stream().collect(Collectors.toMap(Option::getId, Function.identity()))
                    : new HashMap<>();

            List<Option> optionsToKeep = new ArrayList<>();

            if (qReq.getOptions() != null) {
                for (OptionRequest oReq : qReq.getOptions()) {
                    Option option;
                    if (oReq.getId() != null) {
                        option = existingOptionsMap.remove(oReq.getId());
                        if (option == null) {
                            throw new SurveyProcessingException("Option with id " + oReq.getId() + " not found in question " + question.getId());
                        }
                    } else {
                        option = new Option();
                        option.setQuestion(question);
                    }
                    option.setText(oReq.getText());
                    option.setOrderNum(oReq.getOrderNum());
                    optionsToKeep.add(option);
                }
            }

            // Xóa các options còn lại trong existingOptionsMap khỏi collection question.getOptions()
            if (question.getOptions() == null) {
                question.setOptions(new ArrayList<>());
            }
            question.getOptions().clear();
            question.getOptions().addAll(optionsToKeep);

            // --- End xử lý options ---

            updatedQuestions.add(questionRepository.save(question));
        }

        // Delete questions that no longer exist
        for (Question q : existingQuestionsMap.values()) {
            questionRepository.delete(q);
        }

        // Update the survey's question list
        survey.setQuestions(updatedQuestions);
        survey = surveyRepository.save(survey);

        // Convert to response DTO
        List<QuestionResponse> questionResponses = updatedQuestions.stream().map(q -> {
            QuestionResponse qResp = new QuestionResponse();
            qResp.setId(q.getId());
            qResp.setQuestion(q.getQuestion());
            qResp.setOrderNum(q.getOrderNum());
            qResp.setType(q.getType());
            qResp.setIsRequired(q.getIsRequired());

            List<OptionResponse> optionResponses = q.getOptions() != null
                    ? q.getOptions().stream().map(opt -> {
                OptionResponse oResp = new OptionResponse();
                oResp.setId(opt.getId());
                oResp.setText(opt.getText());
                oResp.setOrderNum(opt.getOrderNum());
                return oResp;
            }).collect(Collectors.toList())
                    : new ArrayList<>();

            qResp.setOptions(optionResponses);
            return qResp;
        }).collect(Collectors.toList());

        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setId(survey.getId());
        surveyResponse.setTitle(survey.getTitle());
        surveyResponse.setDescription(survey.getDescription());
        surveyResponse.setStartTime(survey.getStartTime());
        surveyResponse.setEndTime(survey.getEndTime());
        surveyResponse.setStatus(survey.getStatus());
        surveyResponse.setCreatedAt(survey.getCreatedAt());
        surveyResponse.setUpdatedAt(survey.getUpdatedAt());
        surveyResponse.setQuestions(questionResponses);

        return surveyResponse;
    }
}
