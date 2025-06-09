package swd392.eventmanagement.model.dto.request;



import lombok.Data;
import swd392.eventmanagement.enums.QuestionType;

import java.util.List;

@Data
public class QuestionRequest {
    private Long id;
    private String question;
    private Integer orderNum;
    private QuestionType type;
    private Boolean isRequired;
    private List<OptionRequest> options;

}
