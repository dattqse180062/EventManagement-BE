package swd392.eventmanagement.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swd392.eventmanagement.enums.RegistrationStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventListRegisteredResponse {
    private EventListAvailableResponse eventInfo;
    private RegistrationStatus registrationStatus;
}
