package swd392.eventmanagement.service.dashboard;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.enums.QuestionType;
import swd392.eventmanagement.exception.AccessDeniedException;
import swd392.eventmanagement.exception.DashboardProcessingException;
import swd392.eventmanagement.exception.EventNotFoundException;
import swd392.eventmanagement.model.dto.response.*;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.Question;
import swd392.eventmanagement.model.entity.Registration;
import swd392.eventmanagement.model.entity.Survey;
import swd392.eventmanagement.repository.*;
import swd392.eventmanagement.service.dashboard.validator.DashboardManageAccessValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

   private final UserRepository userRepository;
   private final EventRepository eventRepository;
   private final RegistrationRepository registrationRepository;
   private final QuestionRepository questionRepository;
   private final AnswerRepository answerRepository;
   private final DashboardManageAccessValidator dashboardManageAccessValidator;
   private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

   @Override
   public DashboardStats getDashboardStats() {
      logger.info("Fetching dashboard statistics");

      try {
         DashboardStats dto = new DashboardStats();
         dto.setTotalUsers(userRepository.countAllUsers());
         dto.setTotalStudents(userRepository.countStudents());
         dto.setTotalLecturers(userRepository.countLectures());
         dto.setTotalEvents(eventRepository.countAllEvents());
         dto.setActiveEvents(eventRepository.countActiveEvents());
         dto.setUpcomingEvents(eventRepository.countUpcomingEvents());
         dto.setTotalRegistrations(registrationRepository.countALlRegistrations());

         long totalRegistrations = registrationRepository.countALlRegistrations();
         long attendees = registrationRepository.countAttendees();

         dto.setParticipationRate(attendees == 0 ? 0 : (double) attendees / totalRegistrations);

         logger.info("Dashboard statistics fetched successfully");
         return dto;
      } catch (Exception e) {
         logger.error("Error while fetching dashboard stats", e);
         throw new DashboardProcessingException("Failed to fetch dashboard statistics", e);
      }
   }

   @Override
   public List<MonthlyEventCount> getEventsByMonth(int year) {
      logger.info("Fetching monthly event counts for year {}", year);

      try {
         List<Object[]> monthlyCounts = eventRepository.countEventsByMonth(year);
         List<MonthlyEventCount> monthlyList = new ArrayList<>();
         for (Object[] row : monthlyCounts) {
            MonthlyEventCount m = new MonthlyEventCount();
            m.setMonth(((Number) row[0]).intValue());
            m.setCount(((Number) row[1]).longValue());
            monthlyList.add(m);
         }
         logger.info("Monthly event counts fetched successfully for year {}", year);
         return monthlyList;
      } catch (Exception e) {
         logger.error("Error while fetching monthly event counts for year {}", year, e);
         throw new DashboardProcessingException("Failed to fetch monthly event counts", e);
      }
   }

   @Override
   public Map<String, Long> getEventTypesDistribution(int year) {
      logger.info("Fetching event types distribution for year {}", year);

      try {
         List<Object[]> typeCounts = eventRepository.countEventTypesByYear(year);
         Map<String, Long> typeMap = new HashMap<>();

         for (Object[] row : typeCounts) {
            String eventType = row[0].toString();
            Long count = (row[1] instanceof Number) ? ((Number) row[1]).longValue() : Long.parseLong(row[1].toString());
            typeMap.put(eventType, count);
         }

         logger.info("Event types distribution fetched successfully for year {}", year);
         return typeMap;
      } catch (Exception e) {
         logger.error("Error while fetching event types distribution for year {}", year, e);
         throw new DashboardProcessingException("Failed to fetch event types distribution", e);
      }


   }

   @Override
   public EventDashBoardResponse getEventDashboard(Long eventId, String departmentCode) {
      try {
         logger.info("Fetching dashboard for eventId: {} with departmentCode: {}", eventId, departmentCode);

         // 1. Validate access
         dashboardManageAccessValidator.validateUserDepartmentAccess(departmentCode);
         dashboardManageAccessValidator.validateEventBelongsToUserDepartment(eventId, departmentCode);
         logger.debug("Access validation passed for departmentCode: {}", departmentCode);

         // 2. Fetch event
         Event event = eventRepository.findById(eventId)
                 .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));
         logger.debug("Fetched event: {}", event.getName());

         // 3. Fetch registrations
         List<Registration> registrations = registrationRepository.findByEventId(eventId);
         logger.debug("Total registrations found: {}", registrations.size());

         int totalRegistered = registrations.size();

         int studentCount = (int) registrations.stream()
                 .filter(r -> r.getUser().getRoles().stream()
                         .anyMatch(role -> "ROLE_STUDENT".equalsIgnoreCase(role.getName())))
                 .count();

         int lecturerCount = (int) registrations.stream()
                 .filter(r -> r.getUser().getRoles().stream()
                         .anyMatch(role -> "ROLE_LECTURER".equalsIgnoreCase(role.getName())))
                 .count();

         int attendedCount = (int) registrations.stream()
                 .filter(Registration::getAttended)
                 .count();

         double attendedRate = totalRegistered == 0 ? 0 : (attendedCount * 100.0) / totalRegistered;

         logger.debug("Student: {}, Lecturer: {}, Attended: {}, Rate: {}%", studentCount, lecturerCount, attendedCount, attendedRate);

         // 4. Survey statistics
         List<SurveyQuestionStatistic> surveyStats = new ArrayList<>();
         Survey survey = event.getSurvey();

         if (survey != null) {
            logger.debug("Survey found with ID: {}", survey.getId());
            List<Question> questions = questionRepository.findBySurveyId(survey.getId());

            for (Question question : questions) {

               if (question.getType() == QuestionType.TEXT) {
                  int textAnswerCount = answerRepository.countNonEmptyTextAnswers(question.getId());

                  List<SurveyOptionStatistic> optionStats = List.of(
                          new SurveyOptionStatistic("Number of responses", textAnswerCount, 0.0)
                  );

                  surveyStats.add(new SurveyQuestionStatistic(
                          question.getQuestion(),
                          question.getType().name(),
                          optionStats
                  ));
                  continue;
               }


               int totalAnswers = answerRepository.countByQuestionId(question.getId());

               List<SurveyOptionStatistic> optionStats = question.getOptions().stream()
                       .map(opt -> {
                          int count = answerRepository.countByOptionId(opt.getId());
                          double percentage = totalAnswers == 0 ? 0.0 : (count * 100.0) / totalAnswers;
                          return new SurveyOptionStatistic(opt.getText(), count, percentage);
                       })
                       .collect(Collectors.toList());

               surveyStats.add(new SurveyQuestionStatistic(
                       question.getQuestion(),
                       question.getType().name(),
                       optionStats
               ));
            }
         }

         // 5. Build response
         EventDashBoardResponse response = new EventDashBoardResponse();
         response.setTotalRegistered(totalRegistered);
         response.setStudentCount(studentCount);
         response.setLecturerCount(lecturerCount);
         response.setAttendedCount(attendedCount);
         response.setAttendedRate(attendedRate);
         response.setSurveyQuestions(surveyStats);

         logger.info("Dashboard data successfully built for eventId: {}", eventId);
         return response;

      } catch (EventNotFoundException | AccessDeniedException ex) {
         logger.warn("Authorization or event error: {}", ex.getMessage());
         throw ex;
      } catch (Exception ex) {
         logger.error("Unexpected error while building dashboard for eventId {}: {}", eventId, ex.getMessage(), ex);
         throw new DashboardProcessingException("Failed to fetch event dashboard", ex);
      }
   }


   }



