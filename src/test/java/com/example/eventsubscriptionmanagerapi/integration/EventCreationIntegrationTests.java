package com.example.eventsubscriptionmanagerapi.integration;

import com.example.eventsubscriptionmanagerapi.controllers.EventsController;
import com.example.eventsubscriptionmanagerapi.dtos.ErrorMessageDTO;
import com.example.eventsubscriptionmanagerapi.dtos.EventCreationDTO;
import com.example.eventsubscriptionmanagerapi.models.Event;
import com.example.eventsubscriptionmanagerapi.repositories.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class EventCreationIntegrationTests {
    @Container
    static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private EventsController eventsController;

    @Autowired
    private EventRepository eventRepository;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresqlContainer::getDriverClassName);
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
    }

    @Test
    void shouldCreateEvent() {
        var eventCreationDTO = new EventCreationDTO("Test Event", "This is a test event", 100F, "2023-10-01", "2023-10-02", "Subscribe to this event");

        var createdEventResponse = eventsController.create(eventCreationDTO);
        var createdEvent = (Event) createdEventResponse.getBody();

        assertNotNull(createdEvent);
        assertTrue(eventRepository.existsById(createdEvent.getId()));
    }

    @Test
    void shouldThrowAlreadyExistingTitle_whenCreatingEventWithExistingTitle() {
        var originalEvent = Event.builder().title("Test Event").address("Test address").price(100F).startsAt(LocalDate.parse("2023-10-01")).endsAt(LocalDate.parse("2023-10-02")).advertisingContent("Subscribe to this event").build();
        eventRepository.save(originalEvent);
        var duplicateTitleEventDto = new EventCreationDTO("Test Event", "This is a test event", 100F, "2023-10-01", "2023-10-02", "Subscribe to this event");

        var response = eventsController.create(duplicateTitleEventDto);
        var responseBody = (ErrorMessageDTO) response.getBody();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals("Event with title Test Event already exists", responseBody.message());
    }

    @Test
    void shouldThrowInvalidEventDuration_whenCreatingEventWithStartsAtBeingAfterEndsAt() {
        var invalidEventDto = new EventCreationDTO("Test Event", "This is a test event", 100F, "2023-10-02", "2023-10-01", "Subscribe to this event");

        var response = eventsController.create(invalidEventDto);
        var responseBody = (ErrorMessageDTO) response.getBody();


        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals("Event start date cannot be after end date", responseBody.message());
    }

    @Test
    void shouldThrowInvalidEventPrice_whenCreatingEventWithNegativePrice() {
        var invalidEventDto = new EventCreationDTO("Test Event", "This is a test event", -100F, "2023-10-01", "2023-10-02", "Subscribe to this event");

        var response = eventsController.create(invalidEventDto);
        var responseBody = (ErrorMessageDTO) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals("Event price must be greater than 0", responseBody.message());
    }

    @Test
    void shouldThrowInvalidEventPrice_whenCreatingEventWithZeroPrice() {
        var invalidEventDto = new EventCreationDTO("Test Event", "This is a test event", 0F, "2023-10-01", "2023-10-02", "Subscribe to this event");

        var response = eventsController.create(invalidEventDto);
        var responseBody = (ErrorMessageDTO) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals("Event price must be greater than 0", responseBody.message());
    }
}
