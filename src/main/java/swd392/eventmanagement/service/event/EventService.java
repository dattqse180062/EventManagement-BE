package swd392.eventmanagement.service.event;

import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.List;

import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.enums.TargetAudience;
import swd392.eventmanagement.model.dto.request.EventCreateRequest;
import swd392.eventmanagement.model.dto.request.EventUpdateRequest;
import swd392.eventmanagement.model.dto.response.EventDetailsDTO;
import swd392.eventmanagement.model.dto.response.EventDetailsManagementDTO;
import swd392.eventmanagement.model.dto.response.EventListDTO;
import swd392.eventmanagement.model.dto.response.EventListManagementDTO;
import swd392.eventmanagement.model.dto.response.EventUpdateStatusResponse;

public interface EventService {
    List<EventListDTO> getAvailableEvents();

    List<EventListDTO> getUserRegisteredEvents();

    List<EventListManagementDTO> getEventsForManagement(String departmentCode);

    EventDetailsDTO getEventDetails(Long eventId);

    EventDetailsManagementDTO getEventDetailsForManagement(String departmentCode, Long eventId);

    List<EventListDTO> searchEvents(
            String name,
            List<Long> tagIds,
            Long typeId,
            TargetAudience targetAudience,
            EventStatus status,
            LocalDateTime from,
            LocalDateTime to,
            EventMode mode,
            Long departmentId);

    EventDetailsManagementDTO createNewEvent(EventCreateRequest eventCreateRequest, String departmentCode);

    EventDetailsManagementDTO updateEvent(Long eventId, EventUpdateRequest eventUpdateRequest, String departmentCode);

    EventUpdateStatusResponse updateEventStatus(Long eventId, EventStatus newStatus, String departmentCode);
}
