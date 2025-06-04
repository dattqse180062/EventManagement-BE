package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import swd392.eventmanagement.model.entity.Option;
import swd392.eventmanagement.model.entity.Question;

import java.util.List;

public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findByQuestion(Question question);

    List<Option> findByQuestionOrderByOrderNumAsc(Question question);

    @Query("SELECT o FROM Option o WHERE o.question.survey.id = ?1")
    List<Option> findBySurveyId(Long surveyId);
}
