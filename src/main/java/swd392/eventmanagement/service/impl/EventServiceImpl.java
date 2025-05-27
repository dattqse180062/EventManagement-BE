package swd392.eventmanagement.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.exception.EventNotFoundException;
import swd392.eventmanagement.exception.EventProcessingException;
import swd392.eventmanagement.model.dto.response.EventListDTO;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.mapper.EventMapper;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.service.EventService;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventMapper eventMapper;

    @Override
    public List<EventListDTO> getAvailableEvents() {
        try {
            List<Event> events = eventRepository.findByStatus(EventStatus.PUBLISHED);

            if (events.isEmpty()) {
                logger.info("No published events found");
                throw new EventNotFoundException("No published events are currently available");
            }

            logger.info("Found {} published events", events.size());
            return eventMapper.toDTOList(events);
        } catch (Exception e) {
            logger.error("Error retrieving available events", e);
            throw new EventProcessingException("Failed to retrieve available events", e);
        }
    }
}
