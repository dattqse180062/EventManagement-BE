package swd392.eventmanagement.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyOptionStatistic {
    private String text;
    private int count;
    private double percentage;
}
