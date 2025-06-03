package swd392.eventmanagement.model.dto.response;

import lombok.Data;

@Data
public class DashboardStats {
    private long totalUsers;
    private long totalStudents;
    private long totalLecturers;
    private long totalEvents;
    private long activeEvents;
    private long upcomingEvents;
    private long totalRegistrations;
    private double participationRate;
}
