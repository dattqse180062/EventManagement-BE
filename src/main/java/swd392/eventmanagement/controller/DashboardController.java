package swd392.eventmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
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

    // 1. Tổng quan dashboard
    @GetMapping("/stats")
    @Operation(
            summary = "Lấy thống kê tổng quan dashboard",
            description = "Trả về các thống kê tổng quan như tổng số sự kiện, người dùng,...",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "Thống kê dashboard được trả về thành công",
            content = @Content(schema = @Schema(implementation = DashboardStats.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Người dùng không có quyền truy cập"
    )
    public DashboardStats getDashboardStats() {
        return dashboardService.getDashboardStats();
    }

    // 2. Số lượng event theo tháng của 1 năm
    @GetMapping("/events-by-month")
    @Operation(
            summary = "Lấy số lượng sự kiện theo tháng",
            description = "Trả về số lượng sự kiện từng tháng trong một năm cụ thể",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "Danh sách sự kiện theo tháng được trả về thành công",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MonthlyEventCount.class)))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Người dùng không có quyền truy cập"
    )
    public List<MonthlyEventCount> getEventsByMonth(@RequestParam int year) {
        return dashboardService.getEventsByMonth(year);
    }

    // 3. Phân bố loại event theo năm
    @GetMapping("/event-types-distribution")
    @Operation(
            summary = "Phân bố loại sự kiện theo năm",
            description = "Trả về phân bố số lượng các loại sự kiện trong một năm cụ thể",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "Phân bố loại sự kiện được trả về thành công",
            content = @Content(schema = @Schema(implementation = Map.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Người dùng không có quyền truy cập"
    )
    public Map<String, Long> getEventTypesDistribution(@RequestParam int year) {
        return dashboardService.getEventTypesDistribution(year);
    }

}
