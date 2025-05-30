package swd392.eventmanagement.model.dto.response;

import java.time.LocalDateTime;

import lombok.Data;
import swd392.eventmanagement.enums.RegistrationStatus;

@Data
public class RegistrationCancelResponse {
    private Long id;
    private String email;
    private String name;
    private String eventName;
    private RegistrationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
}
