package swd392.eventmanagement.model.dto.response;

import lombok.Data;
import swd392.eventmanagement.enums.RegistrationStatus;

@Data
public class RegistrationCreateResponse {
    private Long id;
    private String email;
    private String name;
    private String eventName;
    private RegistrationStatus status;
    private String checkinUrl;
    private String createdAt;
}
