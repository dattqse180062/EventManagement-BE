package swd392.eventmanagement.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDashBoardResponse {

    //Participation
    private int totalRegistered;
    private int studentCount;
    private int lecturerCount;
    private int attendedCount;
    private double attendedRate;

    //Survey statistics
    private List<SurveyQuestionStatistic> surveyQuestions;



}
