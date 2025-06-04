package swd392.eventmanagement.service.event.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.repository.EventRepository;

@Component
public class EventStatusScheduler {
    private static final Logger logger = LoggerFactory.getLogger(EventStatusScheduler.class);
    private final EventRepository eventRepository;

    public EventStatusScheduler(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();
        logger.debug("Running scheduled event status update at: {}", now); // 1. Update PUBLISHED events to CLOSED if
                                                                           // registration period has ended
        List<Event> toClose = eventRepository.findAllByStatusAndRegistrationEndBefore(EventStatus.PUBLISHED, now);
        if (!toClose.isEmpty()) {
            toClose.forEach(event -> {
                event.setStatus(EventStatus.CLOSED);
                logger.info("Auto-closing event: {} (ID: {})", event.getName(), event.getId());
            });
            eventRepository.saveAll(toClose);
        }

        // 2. Update CLOSED events to COMPLETED if event has ended
        List<Event> toComplete = eventRepository.findAllByStatusAndEndTimeBefore(EventStatus.CLOSED, now);
        if (!toComplete.isEmpty()) {
            toComplete.forEach(event -> {
                event.setStatus(EventStatus.COMPLETED);
                logger.info("Auto-completing event: {} (ID: {})", event.getName(), event.getId());
            });
            eventRepository.saveAll(toComplete);
        }
    }
}
