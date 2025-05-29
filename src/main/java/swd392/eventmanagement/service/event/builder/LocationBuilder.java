package swd392.eventmanagement.service.event.builder;

import org.springframework.stereotype.Component;

import swd392.eventmanagement.model.dto.request.LocationCreateRequest;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.Location;
import swd392.eventmanagement.repository.LocationRepository;

@Component
public class LocationBuilder {

    private final LocationRepository locationRepository;

    public LocationBuilder(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    /**
     * Creates a location from location create request
     */
    public Location createLocation(LocationCreateRequest locationRequest) {
        Location location = new Location();
        location.setAddress(locationRequest.getAddress());
        location.setWard(locationRequest.getWard());
        location.setDistrict(locationRequest.getDistrict());
        location.setCity(locationRequest.getCity());
        return locationRepository.save(location);
    }

    /**
     * Updates or creates a location for an event based on the request
     * Using PUT semantics - if location is null, existing location is deleted
     * 
     * @param event           Event entity to update
     * @param locationRequest Location data from request, can be null
     */
    public void updateEventLocation(Event event, LocationCreateRequest locationRequest) {
        if (locationRequest != null) {
            // If event already has a location, update it; otherwise create a new one
            Location location;
            if (event.getLocation() != null) {
                location = event.getLocation();
                location.setAddress(locationRequest.getAddress());
                location.setWard(locationRequest.getWard());
                location.setDistrict(locationRequest.getDistrict());
                location.setCity(locationRequest.getCity());
                locationRepository.save(location);
            } else {
                location = createLocation(locationRequest);
                event.setLocation(location);
            }
        } else {
            // For PUT, if no location provided, remove existing location from database
            if (event.getLocation() != null) {
                Location locationToDelete = event.getLocation();
                event.setLocation(null);
                locationRepository.delete(locationToDelete);
            }
        }
    }
}
