package swd392.eventmanagement.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd392.eventmanagement.model.dto.request.SurveyCreateRequest;
import swd392.eventmanagement.model.dto.request.SurveyUpdateRequest;
import swd392.eventmanagement.model.dto.response.SurveyResponse;
import swd392.eventmanagement.service.survey.impl.SurveyServiceImpl;

@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
@Tag(name = "Survey", description = "Survey API")
public class SurveyController {

    private final SurveyServiceImpl surveyService;



    @Operation(
            summary = "Create a new survey with questions",
            description = "Create a survey along with multiple questions in a single request",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Survey created successfully",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("")
    public ResponseEntity<SurveyResponse> createSurveyWithQuestions(
            @RequestParam("departmentCode") String departmentCode,
            @Valid @RequestBody SurveyCreateRequest request
    ) {
        SurveyResponse response = surveyService.createSurveyWithQuestions(request, departmentCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Update an existing survey with questions",
            description = "Update a survey and its questions, including adding, modifying, or deleting questions and options",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Survey updated successfully",
                    content = @Content(schema = @Schema(implementation = SurveyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Survey not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{surveyId}")
    public ResponseEntity<SurveyResponse> updateSurveyWithQuestions(
            @PathVariable("surveyId") Long surveyId,
            @RequestParam("departmentCode") String departmentCode,
            @Valid @RequestBody SurveyUpdateRequest request
    ) {
        SurveyResponse response = surveyService.updateSurveyWithQuestions(surveyId, request, departmentCode);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "View draft survey by event ID",
            description = "Retrieve a survey in DRAFT status along with its questions and options by event ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Draft survey retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Survey is not in DRAFT status"),
            @ApiResponse(responseCode = "404", description = "Event or Survey not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/events/{eventId}/survey/draft")
    public ResponseEntity<SurveyResponse> getDraftSurveyByEvent(@PathVariable Long eventId) {
        SurveyResponse surveyResponse = surveyService.viewSurveyDetailByEventIdAndDraftStatus(eventId);
        return ResponseEntity.ok(surveyResponse);
    }

    @Operation(
            summary = "View opened survey by event ID",
            description = "Retrieve a public (OPENED) survey along with its questions and options by event ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opened survey retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Survey is not in OPENED status"),
            @ApiResponse(responseCode = "404", description = "Event or Survey not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/events/{eventId}/survey/opened")
    public ResponseEntity<SurveyResponse> getOpenedSurveyByEvent(@PathVariable Long eventId) {
        SurveyResponse surveyResponse = surveyService.viewSurveyDetailByEventIdAndOpenStatus(eventId);
        return ResponseEntity.ok(surveyResponse);
    }

    @Operation(
            summary = "Delete a survey",
            description = "Delete a survey along with its questions and options by survey ID. " +
                    "Only users with HEAD role can perform this action on surveys belonging to their own department.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Survey deleted successfully",
                    content = @Content(schema = @Schema(example = "Survey deleted successfully"))),
            @ApiResponse(responseCode = "403", description = "Access denied - User does not have permission for the specified department",
                    content = @Content(schema = @Schema(example = "Access denied: department mismatch"))),
            @ApiResponse(responseCode = "404", description = "Survey not found",
                    content = @Content(schema = @Schema(example = "Survey not found with id: 123"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(example = "Failed to remove survey with id: 123")))
    })
    @DeleteMapping("/{surveyId}")
    public ResponseEntity<String> deleteSurvey(
            @PathVariable Long surveyId,
            @RequestParam("eventId") Long eventId,
            @RequestParam("departmentCode") String departmentCode
    ) {
        surveyService.removeSurvey(surveyId, eventId, departmentCode);
        return ResponseEntity.ok("Survey deleted successfully");
    }












}