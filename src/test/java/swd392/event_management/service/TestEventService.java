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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import swd392.eventmanagement.enums.EventStatus;
import swd392.eventmanagement.model.dto.response.EventListDTO;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.mapper.EventMapper;
import swd392.eventmanagement.repository.EventRepository;
import swd392.eventmanagement.security.service.UserDetailsImpl;
import swd392.eventmanagement.service.impl.EventServiceImpl;

public class TestEventService {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UserDetailsImpl userDetails;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        // Clear the security context after each test
        SecurityContextHolder.clearContext();
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

    @Test
    @DisplayName("Test getting user registered events")
    public void testGetUserRegisteredEvents() {
        // Prepare test data
        List<Event> mockEvents = createMockEvents();
        Long userId = 1L;

        // Create mock DTOs using the real DTO class
        EventListDTO dto1 = new EventListDTO();
        EventListDTO dto2 = new EventListDTO();
        List<EventListDTO> mockDTOs = Arrays.asList(dto1, dto2); // Mock security context to return the test user
        when(userDetails.getId()).thenReturn(userId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock repository behavior
        when(eventRepository.findEventsByUserId(userId)).thenReturn(mockEvents);

        // Mock mapper behavior
        when(eventMapper.toDTOList(mockEvents)).thenReturn(mockDTOs);

        // Get registered events through service
        List<EventListDTO> eventDTOs = eventService.getUserRegisteredEvents();

        // Verify events are retrieved and have data
        assertNotNull(eventDTOs, "Event DTOs should not be null");
        assertEquals(2, eventDTOs.size(), "Should return 2 registered event DTOs");
        System.out.println("Number of registered events found: " + eventDTOs.size());

        // Print information about test execution
        System.out.println("Successfully retrieved and mapped registered events to DTOs");
        System.out.println("Test passed: Found " + eventDTOs.size() + " registered events as expected");
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
