package swd392.eventmanagement.model.dto.request;

import lombok.Data;

@Data
public class RegistrationCreateRequest {
    private String email;
    private String name;
    private Long eventId;
}
