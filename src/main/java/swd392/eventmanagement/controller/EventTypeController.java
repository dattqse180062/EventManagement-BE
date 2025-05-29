package swd392.eventmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import swd392.eventmanagement.model.dto.response.EventTypeDTO;
import swd392.eventmanagement.service.EventTypeService;

@RestController
@RequestMapping("/api/v1/event-types")
@RequiredArgsConstructor
@Tag(name = "EventType", description = "Event Type API")
public class EventTypeController {

    private final EventTypeService eventTypeService;

    @GetMapping
    @Operation(summary = "Get all event types", description = "Returns a list of all available event types")
    @ApiResponse(responseCode = "200", description = "Event types retrieved successfully", content = @Content(schema = @Schema(implementation = EventTypeDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "No event types found in the system")
    public ResponseEntity<?> getAllEventTypes() {
        return ResponseEntity.ok(eventTypeService.getAllEventTypes());
    }
}
