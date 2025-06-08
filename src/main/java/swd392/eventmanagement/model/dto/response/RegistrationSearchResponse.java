package swd392.eventmanagement.model.dto.response;

import java.time.LocalDateTime;

import lombok.Data;
import swd392.eventmanagement.enums.RegistrationStatus;

@Data
public class RegistrationSearchResponse {
    private Long id;
    private String eventId;
    private String eventName;
    private String email;
    private String name;
    private RegistrationStatus registrationStatus;
    private LocalDateTime registeredAt;
    private LocalDateTime checkTime;
}
