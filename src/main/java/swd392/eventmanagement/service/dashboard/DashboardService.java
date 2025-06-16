package swd392.eventmanagement.service.dashboard;

import swd392.eventmanagement.model.dto.response.DashboardStats;
import swd392.eventmanagement.model.dto.response.EventDashBoardResponse;
import swd392.eventmanagement.model.dto.response.MonthlyEventCount;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    DashboardStats getDashboardStats();
    List<MonthlyEventCount> getEventsByMonth(int year);
    Map<String, Long> getEventTypesDistribution(int year);

    EventDashBoardResponse getEventDashboard(Long eventId, String departmentCode);
}
