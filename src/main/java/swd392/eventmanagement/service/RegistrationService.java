package swd392.eventmanagement.service;

import swd392.eventmanagement.model.dto.request.RegistrationCreateRequest;
import swd392.eventmanagement.model.dto.response.RegistrationCreateResponse;

public interface RegistrationService {
    RegistrationCreateResponse createRegistration(RegistrationCreateRequest request);
}
