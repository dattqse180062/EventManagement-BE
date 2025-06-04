package swd392.eventmanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.exception.DashboardProcessingException;
import swd392.eventmanagement.model.dto.response.DashboardStats;
import swd392.eventmanagement.model.dto.response.MonthlyEventCount;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.repository.RegistrationRepository;
import swd392.eventmanagement.repository.UserRepository;
import swd392.eventmanagement.service.DashboardService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

   private final UserRepository userRepository;
   private final EventRepository eventRepository;
   private final RegistrationRepository registrationRepository;
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
}
