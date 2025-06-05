package swd392.eventmanagement.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.exception.SurveyNotFoundException;
import swd392.eventmanagement.exception.SurveyProcessingException;
import swd392.eventmanagement.model.dto.request.QuestionCreateRequest;
import swd392.eventmanagement.model.dto.response.OptionResponse;
import swd392.eventmanagement.model.dto.response.QuestionResponse;
import swd392.eventmanagement.model.entity.Option;
import swd392.eventmanagement.model.entity.Question;
import swd392.eventmanagement.model.entity.Survey;
import swd392.eventmanagement.repository.QuestionRepository;
import swd392.eventmanagement.repository.SurveyRepository;
import swd392.eventmanagement.service.SurveyService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {
    private static final Logger logger = LoggerFactory.getLogger(SurveyServiceImpl.class);
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;

    @Override
    @Transactional
    public List<QuestionResponse> createQuestionForSurvey(Long surveyId, List<QuestionCreateRequest> requestList) {
        logger.info("Creating questions for surveyId: {}", surveyId);

        try {
            // Find the survey by id, throw exception if not found
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new SurveyNotFoundException("Survey not found with id = " + surveyId));

            // Step 1: Check for duplicate questions inside the incoming request list
            Set<String> normalizedQuestionsInRequest = new HashSet<>();
            for (QuestionCreateRequest qReq : requestList) {
                String normalized = qReq.getQuestion().trim().toLowerCase();
                if (!normalizedQuestionsInRequest.add(normalized)) {
                    throw new RuntimeException("Duplicate question in request list: \"" + qReq.getQuestion() + "\"");
                }
            }

            // Step 2: Check for duplicate questions already existing in the database for this survey
            List<Question> existingQuestions = questionRepository.findBySurveyId(surveyId);
            Set<String> existingQuestionTexts = existingQuestions.stream()
                    .map(q -> q.getQuestion().trim().toLowerCase())
                    .collect(Collectors.toSet());

            for (QuestionCreateRequest qReq : requestList) {
                String normalized = qReq.getQuestion().trim().toLowerCase();
                if (existingQuestionTexts.contains(normalized)) {
                    throw new RuntimeException("Question already exists in survey: \"" + qReq.getQuestion() + "\"");
                }
            }

            List<QuestionResponse> responseList = new ArrayList<>();

            for (QuestionCreateRequest qReq : requestList) {
                // Create new question entity and set properties
                Question question = new Question();
                question.setSurvey(survey);
                question.setQuestion(qReq.getQuestion());
                question.setOrderNum(qReq.getOrderNum());
                question.setType(qReq.getType());
                question.setIsRequired(qReq.getIsRequired());

                // If options exist, create and assign them before saving question
                if (qReq.getOptions() != null && !qReq.getOptions().isEmpty()) {
                    List<Option> options = qReq.getOptions().stream().map(optReq -> {
                        Option option = new Option();
                        option.setText(optReq.getText());
                        option.setOrderNum(optReq.getOrderNum());
                        option.setQuestion(question);  // set question to option
                        return option;
                    }).collect(Collectors.toList());

                    question.setOptions(options);  // assign options to question before save
                }

                // Save question (cascade will save options as well)
                Question savedQuestion = questionRepository.save(question);

                // Prepare response data
                QuestionResponse resp = new QuestionResponse();
                resp.setId(savedQuestion.getId());
                resp.setQuestion(savedQuestion.getQuestion());
                resp.setOrderNum(savedQuestion.getOrderNum());
                resp.setType(savedQuestion.getType());
                resp.setIsRequired(savedQuestion.getIsRequired());

                List<OptionResponse> optionResponses = savedQuestion.getOptions() != null
                        ? savedQuestion.getOptions().stream().map(opt -> {
                    OptionResponse oResp = new OptionResponse();
                    oResp.setId(opt.getId());
                    oResp.setText(opt.getText());
                    oResp.setOrderNum(opt.getOrderNum());
                    return oResp;
                }).collect(Collectors.toList())
                        : new ArrayList<>();

                resp.setOptions(optionResponses);
                responseList.add(resp);
            }

            logger.info("Created {} questions for surveyId: {}", responseList.size(), surveyId);
            return responseList;

        } catch (Exception e) {
            logger.error("Failed to create questions for surveyId: {}", surveyId, e);
            throw new SurveyProcessingException("Failed to create questions for survey: " + e.getMessage(), e);
        }
    }
}
