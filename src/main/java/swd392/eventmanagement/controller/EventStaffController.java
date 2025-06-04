package swd392.eventmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import swd392.eventmanagement.model.dto.request.StaffManageRequest;
import swd392.eventmanagement.model.dto.response.StaffResponse;
import swd392.eventmanagement.model.dto.response.StaffRoleResponse;
import swd392.eventmanagement.service.event.StaffService;

@RestController
@RequestMapping("/api/v1/staffs")
@Tag(name = "Event Staff", description = "Event Staff API")
public class EventStaffController {

    @Autowired
    private StaffService staffService;

    @GetMapping("/roles")
    @Operation(summary = "Get all staff roles", description = "Lists all available staff roles that can be assigned to event staff", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Successfully retrieved staff roles", content = @Content(schema = @Schema(implementation = StaffRoleResponse.class, type = "array")))
    @ApiResponse(responseCode = "404", description = "Not Found - Role not found")
    public ResponseEntity<?> getAllStaffRoles() {
        return ResponseEntity.ok(staffService.getAllStaffRoles());
    }

    @PostMapping("/management/{departmentCode}/event/{eventId}/assign")
    @Operation(summary = "Assign staff to event", description = "Assigns a staff member to an event with specific roles", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Staff assigned successfully", content = @Content(schema = @Schema(implementation = StaffResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid data supplied")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - Event, user, department, or role not found")
    public ResponseEntity<?> assignStaff(
            @PathVariable String departmentCode,
            @PathVariable Long eventId,
            @RequestBody StaffManageRequest staffCreateRequest) {
        return ResponseEntity.ok(staffService.assignStaff(eventId, departmentCode, staffCreateRequest));
    }

    @PutMapping("/management/{departmentCode}/event/{eventId}/update")
    @Operation(summary = "Update staff roles in event", description = "Updates the roles of a staff member in an event", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Staff roles updated successfully", content = @Content(schema = @Schema(implementation = StaffResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid data supplied")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - Event, user, department, or role not found")
    public ResponseEntity<?> updateStaff(
            @PathVariable String departmentCode,
            @PathVariable Long eventId,
            @RequestBody StaffManageRequest staffUpdateRequest) {
        return ResponseEntity.ok(staffService.updateStaff(eventId, departmentCode, staffUpdateRequest));
    }

    @GetMapping("/management/{departmentCode}/event/{eventId}")
    @Operation(summary = "Get all staff in event", description = "Lists all staff members assigned to an event with their roles", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Successfully retrieved staff list", content = @Content(schema = @Schema(implementation = StaffResponse.class, type = "array")))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - Event or department not found")
    public ResponseEntity<?> getEventStaffs(
            @PathVariable String departmentCode,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(staffService.getEventStaffs(eventId, departmentCode));
    }

    @DeleteMapping("/management/{departmentCode}/event/{eventId}")
    @Operation(summary = "Remove staff from event", description = "Removes a staff member from an event completely", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Staff removed successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - Event, staff, or department not found")
    public ResponseEntity<?> removeStaff(
            @PathVariable String departmentCode,
            @PathVariable Long eventId,
            @RequestParam String staffEmail) {
        staffService.removeStaff(eventId, departmentCode, staffEmail);
        return ResponseEntity.noContent().build();
    }
}
