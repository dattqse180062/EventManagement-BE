package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import swd392.eventmanagement.model.entity.Answer;
import swd392.eventmanagement.model.entity.Option;
import swd392.eventmanagement.model.entity.Question;
import swd392.eventmanagement.model.entity.Response;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByResponse(Response response);

    List<Answer> findByQuestion(Question question);

    Optional<Answer> findByResponseAndQuestion(Response response, Question question);

    List<Answer> findByOption(Option option);

    @Query("SELECT a FROM Answer a WHERE a.response.survey.id = ?1")
    List<Answer> findBySurveyId(Long surveyId);

    @Query("SELECT a FROM Answer a WHERE a.question.id = ?1 AND a.option.id = ?2")
    List<Answer> findByQuestionIdAndOptionId(Long questionId, Long optionId);

    @Query("SELECT COUNT(a) FROM Answer a WHERE a.question = ?1 AND a.option = ?2")
    Long countByQuestionAndOption(Question question, Option option);
}
