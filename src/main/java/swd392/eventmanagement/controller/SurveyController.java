package swd392.eventmanagement.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import swd392.eventmanagement.model.dto.request.QuestionCreateRequest;
import swd392.eventmanagement.model.dto.response.QuestionResponse;
import swd392.eventmanagement.service.impl.SurveyServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
@Tag(name = "Survey", description = "Survey API")
public class SurveyController {

    private final SurveyServiceImpl surveyService;

    @PostMapping("/{surveyId}/questions")

    @Operation(
            summary = "Create questions for a survey",
            description = "Create multiple questions for a given survey ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Questions created successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = QuestionResponse.class))))
    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid question data supplied")
    @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission")
    @ApiResponse(responseCode = "404", description = "Survey not found with given ID")
    public ResponseEntity<List<QuestionResponse>> createQuestionsForSurvey(
            @PathVariable Long surveyId,
            @Valid @RequestBody List<QuestionCreateRequest> requestList
    ) {
        List<QuestionResponse> createdQuestions = surveyService.createQuestionForSurvey(surveyId, requestList);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestions);
    }
}