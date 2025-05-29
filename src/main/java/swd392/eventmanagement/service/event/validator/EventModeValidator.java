package swd392.eventmanagement.service.event.validator;

import org.springframework.stereotype.Component;

import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.exception.EventRequestValidationException;
import swd392.eventmanagement.model.dto.request.LocationCreateRequest;
import swd392.eventmanagement.model.dto.request.PlatformCreateRequest;

@Component
public class EventModeValidator {

    /**
     * Validates location and platform requirements based on event mode
     * 
     * @param mode     The event mode (ONLINE, OFFLINE, HYBRID)
     * @param location The location details (required for OFFLINE and HYBRID)
     * @param platform The platform details (required for ONLINE and HYBRID)
     * @throws EventRequestValidationException if validation fails
     */
    public void validateModeRequirements(
            EventMode mode,
            LocationCreateRequest location,
            PlatformCreateRequest platform) {

        if (mode == null) {
            throw new EventRequestValidationException("Event mode cannot be null");
        }

        switch (mode) {
            case OFFLINE:
                validateOfflineRequirements(location);
                break;
            case ONLINE:
                validateOnlineRequirements(platform);
                break;
            case HYBRID:
                validateHybridRequirements(location, platform);
                break;
            default:
                throw new EventRequestValidationException("Invalid event mode");
        }
    }

    private void validateOfflineRequirements(LocationCreateRequest location) {
        if (location == null) {
            throw new EventRequestValidationException("Location is required for OFFLINE events");
        }
        validateLocation(location);
    }

    private void validateOnlineRequirements(PlatformCreateRequest platform) {
        if (platform == null) {
            throw new EventRequestValidationException("Platform is required for ONLINE events");
        }
        validatePlatform(platform);
    }

    private void validateHybridRequirements(LocationCreateRequest location, PlatformCreateRequest platform) {
        if (location == null) {
            throw new EventRequestValidationException("Location is required for HYBRID events");
        }
        if (platform == null) {
            throw new EventRequestValidationException("Platform is required for HYBRID events");
        }
        validateLocation(location);
        validatePlatform(platform);
    }

    private void validateLocation(LocationCreateRequest location) {
        if (location.getAddress() == null || location.getAddress().trim().isEmpty()) {
            throw new EventRequestValidationException("Location address is required");
        }
        if (location.getWard() == null || location.getWard().trim().isEmpty()) {
            throw new EventRequestValidationException("Location ward is required");
        }
        if (location.getDistrict() == null || location.getDistrict().trim().isEmpty()) {
            throw new EventRequestValidationException("Location district is required");
        }
        if (location.getCity() == null || location.getCity().trim().isEmpty()) {
            throw new EventRequestValidationException("Location city is required");
        }
    }

    private void validatePlatform(PlatformCreateRequest platform) {
        if (platform.getName() == null || platform.getName().trim().isEmpty()) {
            throw new EventRequestValidationException("Platform name is required");
        }
        if (platform.getUrl() == null || platform.getUrl().trim().isEmpty()) {
            throw new EventRequestValidationException("Platform URL is required");
        }
    }
}
