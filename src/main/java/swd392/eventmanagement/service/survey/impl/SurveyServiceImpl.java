package swd392.eventmanagement.service.survey.impl;


import com.opencsv.CSVWriter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.enums.SurveyStatus;
import swd392.eventmanagement.exception.*;
import swd392.eventmanagement.exception.InvalidAnswerException;
import swd392.eventmanagement.model.dto.request.*;
import swd392.eventmanagement.model.dto.response.*;
import swd392.eventmanagement.model.entity.*;
import swd392.eventmanagement.repository.*;
import swd392.eventmanagement.security.service.UserDetailsImpl;
import swd392.eventmanagement.service.survey.SurveyService;
import swd392.eventmanagement.service.survey.validator.SurveyManageAccessValidator;

import java.io.StringWriter;
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
    private final AnswerRepository answerRepository;
    private final ResponseRepository responseRepository;
    private final RegistrationRepository registrationRepository;


    @Override
    public SurveyResponse createSurveyWithQuestions(SurveyCreateRequest request, String departmentCode) {
        logger.info("Creating new survey with title: {}", request.getTitle());

        // Validate user's access to the department and the event
        surveyManageAccessValidator.validateUserDepartmentAccess(departmentCode);
        surveyManageAccessValidator.validateEventBelongsToUserDepartment(request.getEventId(), departmentCode);

        try {
            // Load event or throw if not found
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + request.getEventId()));

            // Reject if event is already completed
            if (event.getStatus() == EventStatus.COMPLETED) {
                throw new SurveyProcessingException("Cannot create survey for a completed event.");
            }

            // Ensure survey start time is within event time range
            if (!(event.getStartTime().isBefore(request.getStartTime()) &&
                    event.getEndTime().isAfter(request.getStartTime()))) {
                throw new SurveyProcessingException("Survey start time must be after event start time and before event end time.");
            }

            // Reject if the event already has a survey assigned
            if (event.getSurvey() != null) {
                throw new SurveyProcessingException("This event already has a survey assigned");
            }

            // Check for duplicate survey by title and time range
            boolean exists = surveyRepository.existsByTitleAndStartTimeAndEndTime(
                    request.getTitle(), request.getStartTime(), request.getEndTime());

            if (exists) {
                throw new SurveyProcessingException("Survey with the same title and time range already exists");
            }

            // Validate duplicate question content
            Set<String> uniqueQuestions = new HashSet<>();
            for (QuestionCreateRequest qReq : request.getQuestions()) {
                String normalized = qReq.getQuestion().trim().toLowerCase();
                if (!uniqueQuestions.add(normalized)) {
                    throw new SurveyProcessingException("Duplicate question: " + normalized);
                }
            }

            // Validate duplicate question order numbers
            Set<Integer> uniqueOrderNums = new HashSet<>();
            for (QuestionCreateRequest qReq : request.getQuestions()) {
                if (!uniqueOrderNums.add(qReq.getOrderNum())) {
                    throw new SurveyProcessingException("Duplicate question order number: " + qReq.getOrderNum());
                }
            }

            // Create and save the Survey
            Survey survey = new Survey();
            survey.setTitle(request.getTitle());
            survey.setDescription(request.getDescription());
            survey.setStartTime(request.getStartTime());
            survey.setEndTime(request.getEndTime());
            survey.setStatus(SurveyStatus.DRAFT);

            Survey savedSurvey = surveyRepository.save(survey);

            // Ensure the createdAt timestamp of the survey is before the event end time
            if (savedSurvey.getCreatedAt().isAfter(event.getEndTime())) {
                throw new SurveyProcessingException("Survey creation time must be before event end time.");
            }

            // Link survey to the event and save
            event.setSurvey(savedSurvey);
            eventRepository.save(event);

            // Save questions and options
            List<QuestionResponse> questionResponses = new ArrayList<>();

            for (QuestionCreateRequest qReq : request.getQuestions()) {
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

                // Build question response DTO
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

            // Build final survey response DTO
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
        surveyManageAccessValidator.validateEventBelongsToUserDepartment(request.getEventId(), departmentCode);

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyNotFoundException("Survey with id " + surveyId + " not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new SurveyProcessingException("Event with id " + request.getEventId() + " not found"));

        // Constraint: Event must not be completed
        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new SurveyProcessingException("Cannot update survey for a completed event.");
        }

        // Constraint: Survey must be in DRAFT status
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new SurveyProcessingException("Only surveys in DRAFT status can be updated.");
        }

        // Constraint: event.endTime > startTime > event.startTime
        if (!(event.getEndTime().isAfter(request.getStartTime()) &&
                request.getStartTime().isAfter(event.getStartTime()))) {
            throw new SurveyProcessingException("Survey start time must be after event start time and before event end time.");
        }

        // Validate duplicate question content
        Set<String> uniqueQuestions = new HashSet<>();
        for (QuestionRequest qReq : request.getQuestions()) {
            String normalizedQuestion = qReq.getQuestion().trim().toLowerCase();
            if (!uniqueQuestions.add(normalizedQuestion)) {
                throw new SurveyProcessingException("Duplicate question: " + normalizedQuestion);
            }
        }

        // Validate duplicate order numbers
        Set<Integer> uniqueOrderNums = new HashSet<>();
        for (QuestionRequest qReq : request.getQuestions()) {
            if (!uniqueOrderNums.add(qReq.getOrderNum())) {
                throw new SurveyProcessingException("Duplicate question order number: " + qReq.getOrderNum());
            }
        }

        // Update survey metadata
        survey.setTitle(request.getTitle());
        survey.setDescription(request.getDescription());
        survey.setStartTime(request.getStartTime());
        survey.setEndTime(request.getEndTime());
        survey = surveyRepository.save(survey);

        // Prepare question updates
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
                            throw new SurveyProcessingException("Option with id " + oReq.getId() +
                                    " not found in question " + question.getId());
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

            if (question.getOptions() == null) {
                question.setOptions(new ArrayList<>());
            }
            question.getOptions().clear();
            question.getOptions().addAll(optionsToKeep);

            updatedQuestions.add(questionRepository.save(question));
        }

        // Remove questions that are no longer present
        for (Question q : existingQuestionsMap.values()) {
            questionRepository.delete(q);
        }

        // Update survey's question list
        survey.setQuestions(updatedQuestions);
        survey = surveyRepository.save(survey);

        // Build response DTO
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


    @Override
    public SurveyResponse viewSurveyDetailByEventId(Long eventId) {
        logger.info("Viewing survey detail for event ID: {}", eventId);

        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

            Survey survey = event.getSurvey();
            if (survey == null) {
                throw new SurveyNotFoundException("Survey not found for event with id: " + eventId);
            }

            logger.info("Survey status for event {}: {}", eventId, survey.getStatus());

            List<QuestionResponse> questionResponses = survey.getQuestions().stream().map(question -> {
                QuestionResponse questionResponse = new QuestionResponse();
                questionResponse.setId(question.getId());
                questionResponse.setQuestion(question.getQuestion());
                questionResponse.setOrderNum(question.getOrderNum());
                questionResponse.setType(question.getType());
                questionResponse.setIsRequired(question.getIsRequired());

                List<OptionResponse> optionResponses = question.getOptions() != null
                        ? question.getOptions().stream().map(opt -> {
                    OptionResponse oResp = new OptionResponse();
                    oResp.setId(opt.getId());
                    oResp.setText(opt.getText());
                    oResp.setOrderNum(opt.getOrderNum());
                    return oResp;
                }).collect(Collectors.toList())
                        : new ArrayList<>();

                questionResponse.setOptions(optionResponses);
                return questionResponse;
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

        } catch (SurveyNotFoundException | EventNotFoundException | AccessDeniedException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Failed to view survey detail for event id: {}", eventId, ex);
            throw new SurveyProcessingException("Failed to view survey detail for event id: " + eventId, ex);
        }
    }


    @Override
    public void removeSurvey(Long surveyId, Long eventId, String departmentCode) {
        try {
            // 1. Validate user access to the specified department
            surveyManageAccessValidator.validateUserDepartmentAccess(departmentCode);

            // 2. Validate that the event belongs to the user's department
            surveyManageAccessValidator.validateEventBelongsToUserDepartment(eventId, departmentCode);

            // 3. Find the survey
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new SurveyNotFoundException("Survey not found with id: " + surveyId));

            // 3.1 Check if survey is in DRAFT status
            if (survey.getStatus() != SurveyStatus.DRAFT) {
                throw new IllegalStateException("Only surveys in DRAFT status can be removed");
            }

            // 4. Unlink the survey from the event (if exists)
            eventRepository.findById(eventId).ifPresent(event -> {
                event.setSurvey(null);
                eventRepository.save(event);
            });

            // 5. Update survey status to CLOSED instead of deleting it
            survey.setStatus(SurveyStatus.CLOSED);
            surveyRepository.save(survey);

            logger.info("Survey with id {} marked as CLOSED and unlinked from event {}", surveyId, eventId);

        } catch (AccessDeniedException | SurveyNotFoundException | EventNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Failed to mark survey as CLOSED with id: {}", surveyId, ex);
            throw new SurveyProcessingException("Failed to remove survey with id: " + surveyId, ex);
        }
    }

    @Override
    public SurveyResponse viewSurveyDetailByEventIdAndOpenStatus(Long eventId) {
        logger.info("Viewing survey detail for event ID: {}", eventId);

        try {

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));


            Survey survey = event.getSurvey();
            if (survey == null) {
                throw new SurveyNotFoundException("Survey not found for event with id: " + eventId);
            }


            if (survey.getStatus() != SurveyStatus.OPENED) {
                throw new AccessDeniedException("Survey for event with id " + eventId + " is not public.");
            }


            List<QuestionResponse> questionResponses = survey.getQuestions().stream().map(question -> {
                QuestionResponse questionResponse = new QuestionResponse();
                questionResponse.setId(question.getId());
                questionResponse.setQuestion(question.getQuestion());
                questionResponse.setOrderNum(question.getOrderNum());
                questionResponse.setType(question.getType());
                questionResponse.setIsRequired(question.getIsRequired());

                List<OptionResponse> optionResponses = question.getOptions() != null
                        ? question.getOptions().stream().map(opt -> {
                    OptionResponse oResp = new OptionResponse();
                    oResp.setId(opt.getId());
                    oResp.setText(opt.getText());
                    oResp.setOrderNum(opt.getOrderNum());
                    return oResp;
                }).collect(Collectors.toList())
                        : new ArrayList<>();

                questionResponse.setOptions(optionResponses);
                return questionResponse;
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

        } catch (SurveyNotFoundException | EventNotFoundException | AccessDeniedException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Failed to view survey detail for event id: {}", eventId, ex);
            throw new SurveyProcessingException("Failed to view survey detail for event id: " + eventId, ex);
        }
    }

    @Transactional
    public void submitSurveyAnswerBySurveyId(SurveySubmissionRequest request) {
        logger.info("Submitting answers for survey ID: {}", request.getSurveyId());

        try {
            // 1. Get the currently authenticated user's ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            Long userId = userDetails.getId();

            // 2. Validate user access and retrieve the corresponding Registration entity
            Registration registration = surveyManageAccessValidator
                    .validateSurveySubmissionAccess(request.getSurveyId(), userId);

            // 3. Get the survey and event from the registration
            Event event = registration.getEvent();
            Survey survey = event.getSurvey();

            // 4. Create a Response entity to represent this submission
            Response response = new Response();
            response.setSurvey(survey);
            response.setRegistration(registration);
            responseRepository.save(response);

            // 5. Build a map of question ID to Question for quick lookup
            Map<Long, Question> questionMap = survey.getQuestions().stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));

            // 5.1 Validate required questions are answered
            Set<Long> answeredQuestionIds = request.getAnswers().stream()
                    .map(QuestionAnswerRequest::getQuestionId)
                    .collect(Collectors.toSet());

            for (Question question : survey.getQuestions()) {
                if (Boolean.TRUE.equals(question.getIsRequired()) && !answeredQuestionIds.contains(question.getId())) {
                    throw new InvalidAnswerException("Missing answer for required question ID: " + question.getId());
                }
            }

            // 6. Iterate over each submitted answer
            for (QuestionAnswerRequest answerRequest : request.getAnswers()) {
                Question question = questionMap.get(answerRequest.getQuestionId());
                if (question == null) {
                    throw new QuestionNotFoundException("Question not found with ID: " + answerRequest.getQuestionId());
                }

                switch (question.getType()) {
                    case TEXT: {
                        if (answerRequest.getAnswerText() == null || answerRequest.getAnswerText().isBlank()) {
                            throw new InvalidAnswerException("Answer text is required for question ID: " + question.getId());
                        }
                        Answer answer = new Answer();
                        answer.setQuestion(question);
                        answer.setResponse(response);
                        answer.setAnswerText(answerRequest.getAnswerText());
                        answerRepository.save(answer);
                        break;
                    }

                    case RATING:
                    case RADIO:
                    case DROPDOWN: {
                        List<OptionAnswerRequest> selected = answerRequest.getSelectedOptions();
                        if (selected == null || selected.size() != 1) {
                            throw new InvalidAnswerException("Exactly one option must be selected for question ID: " + question.getId());
                        }

                        Long optionId = selected.get(0).getOptionId();
                        Option option = optionRepository.findById(optionId)
                                .orElseThrow(() -> new InvalidAnswerException("Invalid option ID: " + optionId));

                        if (!question.getOptions().contains(option)) {
                            throw new InvalidAnswerException("Option ID " + optionId + " does not belong to question ID: " + question.getId());
                        }

                        Answer answer = new Answer();
                        answer.setQuestion(question);
                        answer.setOption(option);
                        answer.setResponse(response);
                        answerRepository.save(answer);
                        break;
                    }

                    case CHECKBOX: {
                        List<OptionAnswerRequest> selected = answerRequest.getSelectedOptions();
                        if (selected == null || selected.isEmpty()) {
                            throw new InvalidAnswerException("At least one option must be selected for question ID: " + question.getId());
                        }

                        for (OptionAnswerRequest optReq : selected) {
                            Long optionId = optReq.getOptionId();
                            Option option = optionRepository.findById(optionId)
                                    .orElseThrow(() -> new InvalidAnswerException("Invalid option ID: " + optionId));

                            if (!question.getOptions().contains(option)) {
                                throw new InvalidAnswerException("Option ID " + optionId + " does not belong to question ID: " + question.getId());
                            }

                            Answer answer = new Answer();
                            answer.setQuestion(question);
                            answer.setOption(option);
                            answer.setResponse(response);
                            answerRepository.save(answer);
                        }
                        break;
                    }

                    default:
                        throw new InvalidAnswerException("Unsupported question type: " + question.getType());
                }
            }

            // 7. Mark that the user has completed the survey
            registration.setSurveyDone(true);
            registrationRepository.save(registration);

            logger.info("Survey answers submitted successfully for user {} and survey {}", userId, survey.getId());

        } catch (Exception ex) {
            logger.error("Error during survey submission for survey ID: {}", request.getSurveyId(), ex);
            throw ex;
        }
    }

    @Override
    public SurveyUserResponse getUserSurveyResponseByResponseId(Long responseId) {
        logger.info("Fetching survey response by response ID: {}", responseId);

        try {
            // 1. Get authenticated user ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            Long userId = userDetails.getId();

            // 2. Fetch response from DB
            Response response = responseRepository.findById(responseId)
                    .orElseThrow(() -> new SurveyProcessingException("Response not found with ID: " + responseId));

            // 3. Validate that the current user owns this response
            Registration registration = response.getRegistration();
            User responseOwner = registration.getUser();

            if (responseOwner == null || !Objects.equals(responseOwner.getId(), userId)) {
                logger.warn("Access denied. Current userId: {}, response owned by: {}", userId,
                        responseOwner != null ? responseOwner.getId() : null);
                throw new AccessDeniedException("You do not have permission to view this response.");
            }

            // 4. Get associated survey
            Survey survey = response.getSurvey();

            // 5. Fetch all answers tied to the response
            List<Answer> answers = answerRepository.findByResponse(response);

            // 6. Map answers to DTO
            List<QuestionAnswerRequest> mappedAnswers = new ArrayList<>();
            for (Answer ans : answers) {
                QuestionAnswerRequest q = new QuestionAnswerRequest();
                q.setQuestionId(ans.getQuestion().getId());

                if (ans.getAnswerText() != null) {
                    q.setAnswerText(ans.getAnswerText());
                } else if (ans.getOption() != null) {
                    q.setSelectedOptions(List.of(new OptionAnswerRequest(ans.getOption().getId())));
                }

                mappedAnswers.add(q);
            }

            // 7. Wrap into response DTO
            SurveyUserResponse dto = new SurveyUserResponse();
            dto.setSurveyId(survey.getId());
            dto.setAnswers(mappedAnswers);

            logger.info("Successfully fetched response ID: {} for user ID: {}", responseId, userId);
            return dto;

        } catch (Exception ex) {
            logger.error("Failed to retrieve survey response by response ID: {}", responseId, ex);
            throw new SurveyProcessingException("Failed to load survey response by ID.", ex);
        }
    }


    @Override
    @Transactional
    public void updateSurveyResponseByResponseId(Long responseId, SurveySubmissionRequest request) {
        logger.info("Updating survey response ID: {}", responseId);

        try {
            // 1. Get authenticated user ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            Long userId = userDetails.getId();

            // 2. Fetch existing response
            Response response = responseRepository.findById(responseId)
                    .orElseThrow(() -> new SurveyProcessingException("Response not found with ID: " + responseId));

            // 3. Validate ownership
            Registration registration = response.getRegistration();
            if (!Objects.equals(registration.getUser().getId(), userId)) {
                logger.warn("Access denied for user {} to update response ID {}", userId, responseId);
                throw new AccessDeniedException("You do not have permission to update this response.");
            }

            Survey survey = response.getSurvey();
            Map<Long, Question> questionMap = survey.getQuestions().stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));

            // 4. Iterate over submitted answers
            for (QuestionAnswerRequest answerRequest : request.getAnswers()) {
                Question question = questionMap.get(answerRequest.getQuestionId());
                if (question == null) {
                    throw new QuestionNotFoundException("Question not found with ID: " + answerRequest.getQuestionId());
                }

                switch (question.getType()) {

                    case TEXT: {
                        if (answerRequest.getAnswerText() == null || answerRequest.getAnswerText().isBlank()) {
                            throw new InvalidAnswerException("Answer text is required for question ID: " + question.getId());
                        }

                        Answer answer = answerRepository.findByResponseAndQuestion(response, question)
                                .orElse(new Answer());

                        answer.setQuestion(question);
                        answer.setResponse(response);
                        answer.setAnswerText(answerRequest.getAnswerText());
                        answer.setOption(null);
                        answerRepository.save(answer);
                        break;
                    }

                    case RATING:
                    case RADIO:
                    case DROPDOWN: {
                        List<OptionAnswerRequest> selected = answerRequest.getSelectedOptions();
                        if (selected == null || selected.size() != 1) {
                            throw new InvalidAnswerException("Exactly one option must be selected for question ID: " + question.getId());
                        }

                        Long optionId = selected.get(0).getOptionId();
                        Option option = optionRepository.findById(optionId)
                                .orElseThrow(() -> new InvalidAnswerException("Invalid option ID: " + optionId));

                        if (!question.getOptions().contains(option)) {
                            throw new InvalidAnswerException("Option ID " + optionId + " does not belong to question ID: " + question.getId());
                        }

                        Answer answer = answerRepository.findByResponseAndQuestion(response, question)
                                .orElse(new Answer());

                        answer.setQuestion(question);
                        answer.setResponse(response);
                        answer.setOption(option);
                        answer.setAnswerText(null);
                        answerRepository.save(answer);
                        break;
                    }

                    case CHECKBOX: {
                        List<OptionAnswerRequest> selected = answerRequest.getSelectedOptions();
                        if (selected == null || selected.isEmpty()) {
                            throw new InvalidAnswerException("At least one option must be selected for question ID: " + question.getId());
                        }

                        // Delete all previous checkbox answers for this question
                        List<Answer> existingAnswers = answerRepository.findAllByResponseAndQuestion(response, question);
                        answerRepository.deleteAll(existingAnswers);

                        for (OptionAnswerRequest optReq : selected) {
                            Long optionId = optReq.getOptionId();
                            Option option = optionRepository.findById(optionId)
                                    .orElseThrow(() -> new InvalidAnswerException("Invalid option ID: " + optionId));

                            if (!question.getOptions().contains(option)) {
                                throw new InvalidAnswerException("Option ID " + optionId + " does not belong to question ID: " + question.getId());
                            }

                            Answer answer = new Answer();
                            answer.setQuestion(question);
                            answer.setOption(option);
                            answer.setResponse(response);
                            answerRepository.save(answer);
                        }
                        break;
                    }

                    default:
                        throw new InvalidAnswerException("Unsupported question type: " + question.getType());
                }
            }

            logger.info("Successfully updated survey response ID: {} by user ID: {}", responseId, userId);

        } catch (Exception ex) {
            logger.error("Failed to update survey response ID: {} - {}", responseId, ex.getMessage(), ex);
            throw new SurveyProcessingException("Failed to update survey response.", ex);
        }
    }


    @Override
    public List<SurveyResponseDetail> getSurveyResponsesByDepartmentAndSurvey(Long surveyId, String departmentCode) {
        logger.info("Fetching all survey responses for survey ID: {}", surveyId);

        surveyManageAccessValidator.validateUserDepartmentAccess(departmentCode);

        try {
            // 1. Fetch survey
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new SurveyProcessingException("Survey not found with ID: " + surveyId));

            // 2. Fetch all responses tied to this survey
            List<Response> responses = responseRepository.findBySurvey(survey);

            // 3. Fetch all questions once
            List<Question> questions = questionRepository.findBySurveyId(surveyId);
            Map<Long, String> questionTextMap = questions.stream()
                    .collect(Collectors.toMap(Question::getId, Question::getQuestion));

            // 4. Build result list
            List<SurveyResponseDetail> result = new ArrayList<>();
            for (Response resp : responses) {
                SurveyResponseDetail detail = new SurveyResponseDetail();
                detail.setResponseId(resp.getId());

                Registration reg = resp.getRegistration();
                User user = reg.getUser();
                detail.setUserName(user.getFullName());
                detail.setSubmittedAt(resp.getCreatedAt());

                List<Answer> answers = answerRepository.findByResponse(resp);
                Map<String, String> mappedAnswers = new LinkedHashMap<>();

                for (Answer answer : answers) {
                    String questionText = questionTextMap.get(answer.getQuestion().getId());
                    String value = (answer.getAnswerText() != null)
                            ? answer.getAnswerText()
                            : (answer.getOption() != null ? answer.getOption().getText() : ""); // fallback
                    mappedAnswers.put(questionText, value);
                }

                detail.setAnswers(mappedAnswers);
                result.add(detail);
            }

            logger.info("Successfully fetched {} survey responses for survey ID: {}", result.size(), surveyId);
            return result;

        } catch (Exception ex) {
            logger.error("Error while fetching survey details for survey ID: {}", surveyId, ex);
            throw new SurveyProcessingException("Failed to load survey responses", ex);
        }
    }


    @Override
    public ResponseEntity<byte[]> exportSurveyResponsesToCSV(Long surveyId, String departmentCode) {
        logger.info("Exporting survey responses to CSV for survey ID: {} and department code: {}", surveyId, departmentCode);

        // 1. Validate access
        surveyManageAccessValidator.validateUserDepartmentAccess(departmentCode);

        // 2. Fetch responses
        List<SurveyResponseDetail> responses = getSurveyResponsesByDepartmentAndSurvey(surveyId, departmentCode);
        logger.debug("Fetched {} survey responses for export", responses.size());

        try (StringWriter sw = new StringWriter();
             CSVWriter writer = new CSVWriter(sw)) {

            // 3. Extract dynamic question headers
            Set<String> questionHeaders = new LinkedHashSet<>();
            for (SurveyResponseDetail detail : responses) {
                questionHeaders.addAll(detail.getAnswers().keySet());
            }
            logger.debug("Dynamic question headers extracted: {}", questionHeaders);

            // 4. Write CSV headers
            List<String> header = new ArrayList<>();
            header.add("Response ID");
            header.add("User Name");
            header.add("Submitted At");
            header.addAll(questionHeaders);
            writer.writeNext(header.toArray(new String[0]));

            // 5. Write CSV rows
            for (SurveyResponseDetail detail : responses) {
                List<String> row = new ArrayList<>();
                row.add(String.valueOf(detail.getResponseId()));
                row.add(detail.getUserName());
                row.add(detail.getSubmittedAt().toString());

                for (String question : questionHeaders) {
                    row.add(detail.getAnswers().getOrDefault(question, ""));
                }

                writer.writeNext(row.toArray(new String[0]));
            }

            // 6. Prepare response
            byte[] csvBytes = sw.toString().getBytes("UTF-8");
            logger.info("Successfully exported survey responses to CSV ({} bytes)", csvBytes.length);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=survey_responses.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csvBytes);

        } catch (Exception e) {
            logger.error("Error occurred while exporting CSV for survey ID: {}", surveyId, e);
            throw new SurveyProcessingException("Failed to export survey responses to CSV", e);
        }
    }

}
