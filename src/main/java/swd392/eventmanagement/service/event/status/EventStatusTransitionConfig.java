package swd392.eventmanagement.service.event.status;

import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import swd392.eventmanagement.enums.EventStatus;

@Configuration
public class EventStatusTransitionConfig {
    @Bean
    Map<EventStatus, Set<EventStatus>> eventStatusTransitions() {
        return Map.of(
                EventStatus.DRAFT, Set.of(EventStatus.PUBLISHED),
                EventStatus.PUBLISHED, Set.of(EventStatus.CLOSED, EventStatus.BLOCKED, EventStatus.CANCELED),
                EventStatus.CLOSED, Set.of(EventStatus.COMPLETED, EventStatus.CANCELED),
                EventStatus.BLOCKED, Set.of(EventStatus.PUBLISHED, EventStatus.CANCELED),
                EventStatus.CANCELED, Set.of(),
                EventStatus.COMPLETED, Set.of());
    }
}
