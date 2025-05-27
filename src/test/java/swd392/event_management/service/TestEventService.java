package swd392.event_management.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.model.dto.response.EventListDTO;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.mapper.EventMapper;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.service.impl.EventServiceImpl;

public class TestEventService {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test getting available events")
    public void testGetAvailableEvents() {
        // Prepare test data
        List<Event> mockEvents = createMockEvents();

        // Create mock DTOs using the real DTO class
        EventListDTO dto1 = new EventListDTO();
        EventListDTO dto2 = new EventListDTO();
        List<EventListDTO> mockDTOs = Arrays.asList(dto1, dto2);

        // Mock repository behavior
        when(eventRepository.findByStatus(EventStatus.PUBLISHED)).thenReturn(mockEvents);

        // Mock mapper behavior
        when(eventMapper.toDTOList(mockEvents)).thenReturn(mockDTOs);

        // Get events through service
        List<EventListDTO> eventDTOs = eventService.getAvailableEvents();

        // Verify events are retrieved and have data
        assertNotNull(eventDTOs, "Event DTOs should not be null");
        assertEquals(2, eventDTOs.size(), "Should return 2 event DTOs");
        System.out.println("Number of available events found: " + eventDTOs.size());

        // Print information about test execution
        System.out.println("Successfully retrieved and mapped events to DTOs");
        System.out.println("Test passed: Found " + eventDTOs.size() + " events as expected");
    }

    /**
     * Helper method to create mock Event objects for testing
     */
    private List<Event> createMockEvents() {
        // Create first event
        Event event1 = new Event();
        event1.setId(1L);
        event1.setName("Test Conference");
        event1.setStatus(EventStatus.PUBLISHED);

        // Create second event
        Event event2 = new Event();
        event2.setId(2L);
        event2.setName("Test Workshop");
        event2.setStatus(EventStatus.PUBLISHED);

        return Arrays.asList(event1, event2);
    }
}
