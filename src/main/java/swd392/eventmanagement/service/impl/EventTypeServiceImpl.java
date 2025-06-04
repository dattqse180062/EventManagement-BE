package swd392.eventmanagement.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import swd392.eventmanagement.exception.EventTypeNotFoundException;
import swd392.eventmanagement.exception.EventTypeProcessingException;
import swd392.eventmanagement.model.dto.response.EventTypeDTO;
import swd392.eventmanagement.model.entity.EventType;
import swd392.eventmanagement.model.mapper.EventTypeMapper;
import swd392.eventmanagement.repository.EventTypeRepository;
import swd392.eventmanagement.service.EventTypeService;

@Service
public class EventTypeServiceImpl implements EventTypeService {

    private static final Logger logger = LoggerFactory.getLogger(EventTypeServiceImpl.class);
    private final EventTypeRepository eventTypeRepository;
    private final EventTypeMapper eventTypeMapper;

    public EventTypeServiceImpl(EventTypeRepository eventTypeRepository, EventTypeMapper eventTypeMapper) {
        this.eventTypeRepository = eventTypeRepository;
        this.eventTypeMapper = eventTypeMapper;
    }

    @Override
    public List<EventTypeDTO> getAllEventTypes() {
        logger.info("Getting all event types");
        try {
            List<EventType> eventTypes = eventTypeRepository.findAll();

            if (eventTypes.isEmpty()) {
                logger.info("No event types found");
                throw new EventTypeNotFoundException("No event types found in the system");
            }

            logger.info("Found {} event types", eventTypes.size());
            return eventTypeMapper.toListDTO(eventTypes);
        } catch (EventTypeNotFoundException e) {
            // Just rethrow EventTypeNotFoundException to be handled by the global exception
            // handler
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving event types", e);
            throw new EventTypeProcessingException("Failed to retrieve event types", e);
        }
    }

}
