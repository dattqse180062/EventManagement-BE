package swd392.eventmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import swd392.eventmanagement.model.dto.response.DashboardStats;
import swd392.eventmanagement.model.dto.response.MonthlyEventCount;
import swd392.eventmanagement.service.DashboardService;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")

    @Operation(
            summary = "Get dashboard overview statistics",
            description = "Returns overview statistics such as total events, users, etc.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Dashboard statistics successfully returned", content = @Content(schema = @Schema(implementation = DashboardStats.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - user does not have access rights")
    @ApiResponse(responseCode = "404", description = "Dashboard data not found")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        DashboardStats stats = dashboardService.getDashboardStats();
        if (stats == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/events-by-month")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get number of events by month",
            description = "Returns the count of events per month in a specific year",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Monthly event counts successfully returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MonthlyEventCount.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden - user does not have access rights")
    @ApiResponse(responseCode = "404", description = "No events found for the given year")
    public ResponseEntity<List<MonthlyEventCount>> getEventsByMonth(@RequestParam int year) {
        List<MonthlyEventCount> monthlyCounts = dashboardService.getEventsByMonth(year);
        if (monthlyCounts == null || monthlyCounts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(monthlyCounts);
    }

    @GetMapping("/event-types-distribution")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get event types distribution",
            description = "Returns the distribution of event types in a specific year",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Event types distribution successfully returned", content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - user does not have access rights")
    @ApiResponse(responseCode = "404", description = "No event types found for the given year")
    public ResponseEntity<Map<String, Long>> getEventTypesDistribution(@RequestParam int year) {
        Map<String, Long> distribution = dashboardService.getEventTypesDistribution(year);
        if (distribution == null || distribution.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(distribution);
    }

}
