package swd392.eventmanagement.service;

import java.util.List;

import swd392.eventmanagement.model.dto.response.EventTypeDTO;

public interface EventTypeService {

    List<EventTypeDTO> getAllEventTypes();

}
