package swd392.eventmanagement.service.survey.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import swd392.eventmanagement.enums.SurveyStatus;
import swd392.eventmanagement.model.entity.Survey;
import swd392.eventmanagement.repository.SurveyRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SurveyStatusScheduler {

    private final SurveyRepository surveyRepository;

    @Scheduled(fixedRate = 60000)
    public void updateSurveyStatuses() {
        log.info("Running scheduled task to update survey statuses...");

        LocalDateTime now = LocalDateTime.now();
        List<Survey> surveys = surveyRepository.findAll();

        for (Survey survey : surveys) {
            SurveyStatus currentStatus = survey.getStatus();

            if (now.isBefore(survey.getStartTime())) {
                if (currentStatus != SurveyStatus.DRAFT) {
                    survey.setStatus(SurveyStatus.DRAFT);
                    surveyRepository.save(survey);
                    log.info("Survey [{}] moved to DRAFT", survey.getId());
                }
            } else if (now.isAfter(survey.getEndTime())) {
                if (currentStatus != SurveyStatus.CLOSED) {
                    survey.setStatus(SurveyStatus.CLOSED);
                    surveyRepository.save(survey);
                    log.info("Survey [{}] moved to CLOSED", survey.getId());
                }
            } else if (now.isAfter(survey.getStartTime()) && now.isBefore(survey.getEndTime())) {
                if (currentStatus != SurveyStatus.OPENED) {
                    survey.setStatus(SurveyStatus.OPENED);
                    surveyRepository.save(survey);
                    log.info("Survey [{}] moved to OPENED", survey.getId());
                }
            }
        }
    }
}
