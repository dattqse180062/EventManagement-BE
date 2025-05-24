package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.eventmanagement.enums.SurveyStatus;
import swd392.eventmanagement.model.entity.Survey;

import java.time.LocalDateTime;
import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findByStatus(SurveyStatus status);

    List<Survey> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Survey> findByEndTimeBefore(LocalDateTime dateTime);
}
