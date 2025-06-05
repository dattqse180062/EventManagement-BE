package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.eventmanagement.enums.QuestionType;
import swd392.eventmanagement.model.entity.Question;
import swd392.eventmanagement.model.entity.Survey;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySurvey(Survey survey);

    List<Question> findBySurveyAndType(Survey survey, QuestionType type);

    List<Question> findByType(QuestionType type);

    List<Question> findBySurveyAndIsRequiredTrue(Survey survey);

    List<Question> findBySurveyId(Long surveyId);
}
