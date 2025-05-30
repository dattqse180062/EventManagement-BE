package swd392.eventmanagement.model.dto.response;

import lombok.Data;

@Data
public class MonthlyEventCount {
    private int month;
    private long count;
}
