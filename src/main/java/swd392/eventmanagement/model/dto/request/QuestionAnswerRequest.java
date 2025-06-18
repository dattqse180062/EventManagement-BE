package swd392.eventmanagement.model.dto.request;

import lombok.Data;

import java.util.List;



@Data
public class QuestionAnswerRequest {
    private Long questionId;
    private List<OptionAnswerRequest> selectedOptions; // For choices
    private String answerText;                         // For text/paragraph
}
