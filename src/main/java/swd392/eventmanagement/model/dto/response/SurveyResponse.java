package swd392.eventmanagement.model.dto.response;

import lombok.Data;
import swd392.eventmanagement.enums.SurveyStatus;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class SurveyResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SurveyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<QuestionResponse> questions;
}
