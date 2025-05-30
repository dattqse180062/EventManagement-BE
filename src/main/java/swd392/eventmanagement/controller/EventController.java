package swd392.eventmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.model.dto.response.EventDetailsDTO;
import swd392.eventmanagement.model.dto.response.EventDetailsManagementDTO;
import swd392.eventmanagement.model.dto.response.EventListDTO;
import swd392.eventmanagement.model.dto.response.EventListManagementDTO;
import swd392.eventmanagement.model.dto.response.EventUpdateStatusResponse;
import swd392.eventmanagement.service.event.EventService;
import swd392.eventmanagement.model.dto.request.EventCreateRequest;
import swd392.eventmanagement.model.dto.request.EventUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event", description = "Event API")
public class EventController {
    @Autowired
    private EventService eventService;

    @GetMapping("")
    @Operation(summary = "Get available events", description = "Returns a list of all available events", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Available events retrieved successfully", content = @Content(schema = @Schema(implementation = EventListDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - No published events are currently available")
    public ResponseEntity<?> getAvailableEvents() {
        return ResponseEntity.ok(eventService.getAvailableEvents());
    }

    @GetMapping("/registered")
    @Operation(summary = "Get user's registered events", description = "Returns a list of events that the current authenticated user has registered for", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "User's registered events retrieved successfully", content = @Content(schema = @Schema(implementation = EventListDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - No registered events found for the user")
    public ResponseEntity<?> getUserRegisteredEvents() {
        return ResponseEntity.ok(eventService.getUserRegisteredEvents());
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event details", description = "Returns detailed information about a specific event", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Event details retrieved successfully", content = @Content(schema = @Schema(implementation = EventDetailsDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - Event with the specified ID does not exist")
    public ResponseEntity<?> getEventDetails(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventDetails(eventId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter events", description = "Search events by name and filter by tag, type, status, time, mode, department", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Events retrieved successfully", content = @Content(schema = @Schema(implementation = EventListDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - No events found matching the search criteria")
    public ResponseEntity<?> searchEvents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) EventMode mode,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(eventService.searchEvents(
                name, tagIds, typeId, status, from, to, mode, departmentId));
    }

    @GetMapping("/management/{departmentCode}")
    @Operation(summary = "Get events for management", description = "Returns a list of all events for management for a specific department", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Events for management retrieved successfully", content = @Content(schema = @Schema(implementation = EventListManagementDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - No events found for the specified department")
    public ResponseEntity<?> getEventsForManagement(@PathVariable String departmentCode) {
        return ResponseEntity.ok(eventService.getEventsForManagement(departmentCode));
    }

    @GetMapping("/management/{departmentCode}/event/{eventId}")
    @Operation(summary = "Get event details for management", description = "Returns detailed information about a specific event for management", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Event details for management retrieved successfully", content = @Content(schema = @Schema(implementation = EventDetailsManagementDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - No events found for the specified department")
    public ResponseEntity<?> getEventDetailsForManagement(@PathVariable String departmentCode,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventDetailsForManagement(departmentCode, eventId));
    }

    @PostMapping("/management/{departmentCode}/event")
    @Operation(summary = "Create new event", description = "Creates a new event in the specified department", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Event created successfully", content = @Content(schema = @Schema(implementation = EventDetailsManagementDTO.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid event data supplied")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found")
    public ResponseEntity<?> createEvent(
            @PathVariable String departmentCode,
            @Valid @RequestBody EventCreateRequest eventCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createNewEvent(eventCreateRequest, departmentCode));
    }

    @PutMapping("/management/{departmentCode}/event/{eventId}")
    @Operation(summary = "Update existing event", description = "Updates an existing event in the specified department", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Event updated successfully", content = @Content(schema = @Schema(implementation = EventDetailsManagementDTO.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid event data supplied")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - Event not found")
    public ResponseEntity<?> updateEvent(
            @PathVariable String departmentCode,
            @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateRequest eventUpdateRequest) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, eventUpdateRequest, departmentCode));
    }

    @PutMapping("/management/{departmentCode}/event/{eventId}/status")
    @Operation(summary = "Update event status", description = "Updates the status of an existing event in the specified department", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Event status updated successfully", content = @Content(schema = @Schema(implementation = EventUpdateStatusResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid status transition requested")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - Event not found")
    public ResponseEntity<?> updateEventStatus(
            @PathVariable String departmentCode,
            @PathVariable Long eventId,
            @RequestParam EventStatus newStatus) {
        return ResponseEntity.ok(eventService.updateEventStatus(eventId, newStatus, departmentCode));
    }
}
