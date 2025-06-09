package swd392.eventmanagement.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data

public class SurveyUpdateRequest {


    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<QuestionRequest> questions;
}
