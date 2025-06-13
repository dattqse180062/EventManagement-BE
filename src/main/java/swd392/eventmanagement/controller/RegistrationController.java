package swd392.eventmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

import swd392.eventmanagement.model.dto.request.RegistrationCreateRequest;
import swd392.eventmanagement.model.dto.response.RegistrationCancelResponse;
import swd392.eventmanagement.model.dto.response.RegistrationCreateResponse;
import swd392.eventmanagement.model.dto.response.RegistrationSearchResponse;
import swd392.eventmanagement.service.registration.RegistrationService;

@RestController
@RequestMapping("/api/v1/registrations")
@Tag(name = "Registration", description = "Registration API")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @PostMapping("")
    @Operation(summary = "Register for an event", description = "Creates a new registration for the current authenticated user for a specific event", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Registration created successfully", content = @Content(schema = @Schema(implementation = RegistrationCreateResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid registration data supplied")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - Event not found")
    @ApiResponse(responseCode = "409", description = "Conflict - User is already registered or event capacity exceeded")
    public ResponseEntity<?> registerForEvent(@Valid @RequestBody RegistrationCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registrationService.createRegistration(request));
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Cancel event registration", description = "Cancels an existing registration for the current authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Registration cancelled successfully", content = @Content(schema = @Schema(implementation = RegistrationCancelResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Not Found - Event or registration not found")
    @ApiResponse(responseCode = "400", description = "Bad Request - Cancellation not allowed (e.g., too close to event start)")
    public ResponseEntity<?> cancelRegistration(@PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.cancelRegistration(eventId));
    }

    @GetMapping("/{eventId}/search")
    @Operation(summary = "Search registration", description = "Search for a registration by email for an event", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Registration found", content = @Content(schema = @Schema(implementation = RegistrationSearchResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input")
    @ApiResponse(responseCode = "404", description = "Not Found - Event, user or registration not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission to search registrations")
    public ResponseEntity<?> searchRegistration(
            @PathVariable Long eventId,
            @RequestParam @Schema(description = "Email of the participant to search for") String email) {
        return ResponseEntity.ok(registrationService.searchRegistration(eventId, email));
    }

    @PostMapping("/{eventId}/checkin")
    @Operation(summary = "Check in a participant", description = "Check in a registered participant for an event using their email", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Check-in successful", content = @Content(schema = @Schema(implementation = RegistrationCancelResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input or check-in not allowed")
    @ApiResponse(responseCode = "404", description = "Not Found - Event, user or registration not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    public ResponseEntity<?> checkin(
            @PathVariable Long eventId,
            @RequestParam @Schema(description = "Email of the participant to check in") String email) {
        return ResponseEntity.ok(registrationService.checkin(eventId, email));
    }

    @PostMapping("/checkin")
    @Operation(summary = "Check in a participant", description = "Check in a registered participant for an event using encrypted data", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Check-in successful", content = @Content(schema = @Schema(implementation = RegistrationCancelResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid encrypted data or check-in not allowed")
    @ApiResponse(responseCode = "404", description = "Not Found - Event, user or registration not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    public ResponseEntity<?> checkin(
            @RequestParam @Schema(description = "Encrypted data containing email and event ID") String checkinCode) {
        return ResponseEntity.ok(registrationService.checkin(checkinCode));
    }
}
