package swd392.eventmanagement.model.dto.response;

import java.time.LocalDateTime;

import lombok.Data;
import swd392.eventmanagement.enums.RegistrationStatus;

@Data
public class CheckinResponse {
    private Long id;
    private String eventName;
    private String email;
    private String message;
    private RegistrationStatus registrationStatus;
    private LocalDateTime checkinTime;
}
