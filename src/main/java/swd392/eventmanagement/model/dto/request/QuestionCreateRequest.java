package swd392.eventmanagement.model.dto.request;



import lombok.Data;
import swd392.eventmanagement.enums.QuestionType;

import java.util.List;

@Data
public class QuestionCreateRequest {
    private String question;
    private Integer orderNum;
    private QuestionType type;
    private Boolean isRequired;
    private List<OptionCreateRequest> options;

}
