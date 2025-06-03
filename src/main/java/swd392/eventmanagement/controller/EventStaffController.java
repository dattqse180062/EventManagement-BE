package swd392.eventmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import swd392.eventmanagement.model.dto.request.StaffCreateRequest;
import swd392.eventmanagement.model.dto.response.StaffResponse;
import swd392.eventmanagement.service.event.StaffService;

@RestController
@RequestMapping("/api/v1/staff")
@Tag(name = "Event Staff", description = "Event Staff Management API")
public class EventStaffController {

    @Autowired
    private StaffService staffService;

    @PostMapping("/management/{departmentCode}/event/{eventId}/assign")
    @Operation(summary = "Assign staff to event", description = "Assigns a staff member to an event with specific roles", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Staff assigned successfully", content = @Content(schema = @Schema(implementation = StaffResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid data supplied")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - Event, user, department, or role not found")
    public ResponseEntity<?> assignStaff(
            @PathVariable String departmentCode,
            @PathVariable Long eventId,
            @RequestBody StaffCreateRequest staffCreateRequest) {
        return ResponseEntity.ok(staffService.assignStaff(eventId, departmentCode, staffCreateRequest));
    }
}
