package swd392.eventmanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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




    @Override
    public DashboardStats getDashboardStats() {
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

        dto.setParticipationRate(attendees == 0 ? 0 : (double) attendees/totalRegistrations);
        return dto;
    }

    @Override
    public List<MonthlyEventCount> getEventsByMonth(int year) {
       List<Object[]> monthlyCounts = eventRepository.countEventsByMonth(year);
       List<MonthlyEventCount> monthlyList = new ArrayList<>();
       for(Object[] row : monthlyCounts) {
           MonthlyEventCount m = new MonthlyEventCount();
           m.setMonth(((Number)row[0]).intValue());
           m.setCount(((Number)row[1]).longValue());
           monthlyList.add(m);

       }
       return monthlyList;
    }

    @Override
    public Map<String, Long> getEventTypesDistribution(int year) {
        List<Object[]> typeCounts = eventRepository.countEventTypesByYear(year);
        Map<String, Long> typeMap = new HashMap<>();

        for (Object[] row : typeCounts) {

            String eventType = row[0].toString();

            Long count;
            if (row[1] instanceof Number) {
                count = ((Number) row[1]).longValue();
            } else {

                count = Long.parseLong(row[1].toString());
            }

            typeMap.put(eventType, count);
        }

        return typeMap;
    }
}
