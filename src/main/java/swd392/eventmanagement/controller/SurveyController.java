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
}