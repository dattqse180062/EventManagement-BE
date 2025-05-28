package swd392.eventmanagement.service;

import java.time.LocalDateTime;
import java.util.List;

import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.model.dto.response.EventDetailsDTO;
import swd392.eventmanagement.model.dto.response.EventListDTO;

public interface EventService {
    List<EventListDTO> getAvailableEvents();

    List<EventListDTO> getUserRegisteredEvents();

    EventDetailsDTO getEventDetails(Long eventId);

    List<EventListDTO> searchEvents(
            String name,
            List<Long> tagIds,
            Long typeId,
            EventStatus status,
            LocalDateTime from,
            LocalDateTime to,
            EventMode mode,
            Long departmentId);
}
