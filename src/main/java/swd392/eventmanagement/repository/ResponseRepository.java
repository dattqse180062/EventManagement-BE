package swd392.eventmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import swd392.eventmanagement.model.entity.Registration;
import swd392.eventmanagement.model.entity.Response;
import swd392.eventmanagement.model.entity.Survey;

import java.util.List;
import java.util.Optional;

public interface ResponseRepository extends JpaRepository<Response, Long> {
    List<Response> findBySurvey(Survey survey);

    List<Response> findByRegistration(Registration registration);

    Optional<Response> findBySurveyAndRegistration(Survey survey, Registration registration);

    @Query("SELECT COUNT(r) FROM Response r WHERE r.survey = ?1")
    Long countBySurvey(Survey survey);
}
