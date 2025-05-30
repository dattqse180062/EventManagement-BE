package swd392.eventmanagement.model.dto.response;

import lombok.Data;

@Data
public class RegistrationCreateResponse {
    private Long id;
    private String email;
    private String name;
    private String eventName;
    private String checkinUrl;
    private String createdAt;
}
