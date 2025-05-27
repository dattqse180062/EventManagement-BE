package swd392.eventmanagement.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import swd392.eventmanagement.security.service.UserDetailsImpl;

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

    @Override
    public List<EventListDTO> getUserRegisteredEvents() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UsernameNotFoundException("User not authenticated");
        }

        Long userId = ((UserDetailsImpl) auth.getPrincipal()).getId();

        try {
            List<Event> events = eventRepository.findEventsByUserId(userId);

            if (events.isEmpty()) {
                logger.info("No registered events found for user {}", userId);
                throw new EventNotFoundException("No registered events found for the user");
            }

            logger.info("Found {} registered events for user {}", events.size(), userId);
            return eventMapper.toDTOList(events);

        } catch (Exception e) {
            logger.error("Error retrieving registered events for user {}", userId, e);
            throw new EventProcessingException("Failed to retrieve registered events", e);
        }
    }
}
