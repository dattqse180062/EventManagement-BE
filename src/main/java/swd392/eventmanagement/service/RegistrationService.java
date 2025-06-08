package swd392.eventmanagement.service;

import swd392.eventmanagement.model.dto.request.RegistrationCreateRequest;
import swd392.eventmanagement.model.dto.response.CheckinResponse;
import swd392.eventmanagement.model.dto.response.RegistrationCancelResponse;
import swd392.eventmanagement.model.dto.response.RegistrationCreateResponse;
import swd392.eventmanagement.model.dto.response.RegistrationSearchResponse;

public interface RegistrationService {
    RegistrationCreateResponse createRegistration(RegistrationCreateRequest request);

    RegistrationCancelResponse cancelRegistration(Long eventId);

    RegistrationSearchResponse searchRegistration(Long eventId, String email);

    CheckinResponse checkin(Long eventId, String email);
}
