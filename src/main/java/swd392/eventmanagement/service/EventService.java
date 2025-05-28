package swd392.eventmanagement.service;

import java.util.List;

import swd392.eventmanagement.model.dto.response.EventDetailsDTO;
import swd392.eventmanagement.model.dto.response.EventListDTO;

public interface EventService {
    List<EventListDTO> getAvailableEvents();

    List<EventListDTO> getUserRegisteredEvents();

    EventDetailsDTO getEventDetails(Long eventId);
}
