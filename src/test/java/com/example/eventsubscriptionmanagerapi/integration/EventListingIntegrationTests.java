package com.example.eventsubscriptionmanagerapi.integration;

import com.example.eventsubscriptionmanagerapi.controllers.EventsController;
import com.example.eventsubscriptionmanagerapi.dtos.EventCreationDTO;
import com.example.eventsubscriptionmanagerapi.repositories.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class EventListingIntegrationTests {
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
    void shouldListEmptyList_whenNoEvents() {
        var response = eventsController.list();
        var responseBody = response.getBody();

        assertNotNull(responseBody);
        assertTrue(responseBody.isEmpty());
    }

    @Test
    void shouldListEvents_whenEventsExist() {
        var eventCreationDTO = new EventCreationDTO("Test Event", "This is a test event", 100F, "2023-10-01", "2023-10-02");
        eventsController.create(eventCreationDTO);

        var response = eventsController.list();
        var responseBody = response.getBody();

        assertNotNull(responseBody);
        assertFalse(responseBody.isEmpty());
        assertEquals(1, responseBody.size());
    }
}
