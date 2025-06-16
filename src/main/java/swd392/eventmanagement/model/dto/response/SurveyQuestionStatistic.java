package swd392.eventmanagement.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionStatistic {
    private String question;
    private String type; // RADIO, CHECKBOX, TEXT, ...
    private List<SurveyOptionStatistic> options;
}
