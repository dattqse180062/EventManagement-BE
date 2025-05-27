package swd392.eventmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import swd392.eventmanagement.model.dto.response.EventListDTO;
import swd392.eventmanagement.service.EventService;

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

}
