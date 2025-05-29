package swd392.eventmanagement.service.event.builder;

import org.springframework.stereotype.Component;

import swd392.eventmanagement.model.dto.request.PlatformCreateRequest;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.Platform;
import swd392.eventmanagement.repository.PlatformRepository;

@Component
public class PlatformBuilder {

    private final PlatformRepository platformRepository;

    public PlatformBuilder(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    /**
     * Creates a platform from platform create request
     */
    public Platform createPlatform(PlatformCreateRequest platformRequest) {
        Platform platform = new Platform();
        platform.setName(platformRequest.getName());
        platform.setUrl(platformRequest.getUrl());
        return platformRepository.save(platform);
    }

    /**
     * Updates or creates a platform for an event based on the request
     * Using PUT semantics - if platform is null, existing platform is deleted
     * 
     * @param event           Event entity to update
     * @param platformRequest Platform data from request, can be null
     */
    public void updateEventPlatform(Event event, PlatformCreateRequest platformRequest) {
        if (platformRequest != null) {
            // If event already has a platform, update it; otherwise create a new one
            Platform platform;
            if (event.getPlatform() != null) {
                platform = event.getPlatform();
                platform.setName(platformRequest.getName());
                platform.setUrl(platformRequest.getUrl());
                platformRepository.save(platform);
            } else {
                platform = createPlatform(platformRequest);
                event.setPlatform(platform);
            }
        } else {
            // For PUT, if no platform provided, remove existing platform from database
            if (event.getPlatform() != null) {
                Platform platformToDelete = event.getPlatform();
                event.setPlatform(null);
                platformRepository.delete(platformToDelete);
            }
        }
    }
}
