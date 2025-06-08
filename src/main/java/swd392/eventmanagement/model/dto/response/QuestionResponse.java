package swd392.eventmanagement.model.dto.response;

import lombok.Data;
import swd392.eventmanagement.enums.QuestionType;

import java.util.List;

@Data
public class QuestionResponse {
    private Long id;
    private String question;
    private Integer orderNum;
    private QuestionType type;
    private Boolean isRequired;
    private List<OptionResponse> options;
}
