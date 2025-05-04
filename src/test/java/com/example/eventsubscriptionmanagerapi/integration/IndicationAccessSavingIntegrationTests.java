package com.example.eventsubscriptionmanagerapi.integration;

import com.example.eventsubscriptionmanagerapi.controllers.IndicationAccessController;
import com.example.eventsubscriptionmanagerapi.dtos.ErrorMessageDTO;
import com.example.eventsubscriptionmanagerapi.dtos.IndicationAccessSavingDTO;
import com.example.eventsubscriptionmanagerapi.models.Event;
import com.example.eventsubscriptionmanagerapi.models.IndicationAccess;
import com.example.eventsubscriptionmanagerapi.models.Subscription;
import com.example.eventsubscriptionmanagerapi.models.User;
import com.example.eventsubscriptionmanagerapi.repositories.EventRepository;
import com.example.eventsubscriptionmanagerapi.repositories.IndicationAccessRepository;
import com.example.eventsubscriptionmanagerapi.repositories.SubscriptionRepository;
import com.example.eventsubscriptionmanagerapi.repositories.UserRepository;
import com.github.javafaker.Faker;
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

import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class IndicationAccessSavingIntegrationTests {
    @Container
    static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest");
    private final Faker faker = new Faker();
    @Autowired
    private IndicationAccessController indicationAccessController;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IndicationAccessRepository indicationAccessRepository;


    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresqlContainer::getDriverClassName);
    }

    @AfterEach
    void tearDown() {
        indicationAccessRepository.deleteAll();
    }

    @Test
    void shouldThrowEventNotFoundException_whenEventDoesNotExist() {
        var nonExistingEventSlug = Event.builder().title(faker.name().title()).build().getSlug();
        var indicator = userRepository.save(User.builder().name(faker.name().fullName()).email(faker.internet().emailAddress()).build());
        var indicationAccessSavingDTO = new IndicationAccessSavingDTO(nonExistingEventSlug, indicator.getId().toString());

        var response = indicationAccessController.save(indicationAccessSavingDTO);
        var responseBody = (ErrorMessageDTO) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(String.format("Event with slug %s not found", nonExistingEventSlug), responseBody.message());
    }

    @Test
    void shouldThrowIndicatorNotFoundExcepion_whenIndicatorDoesNotExist() {
        var nonExistingIndicatorId = UUID.randomUUID().toString();
        var eventStartsAt = faker.date().future(10, java.util.concurrent.TimeUnit.DAYS);
        var eventsEndsAt = faker.date().future(20, java.util.concurrent.TimeUnit.DAYS, eventStartsAt);
        var event = eventRepository.save(Event.builder().title(faker.name().title()).address(faker.address().fullAddress()).price((float) faker.number().randomDouble(1, 1, 1000)).startsAt(eventStartsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).endsAt(eventsEndsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).build());
        var indicationAccessSavingDTO = new IndicationAccessSavingDTO(event.getSlug(), nonExistingIndicatorId);

        var response = indicationAccessController.save(indicationAccessSavingDTO);
        var responseBody = (ErrorMessageDTO) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(String.format("Indicator with id %s not found", nonExistingIndicatorId), responseBody.message());
    }

    @Test
    void shouldThrowIndicatorNotSubscribedToEvent_whenIndicatorIsNotSubscribedToEvent() {
        var eventStartsAt = faker.date().future(10, java.util.concurrent.TimeUnit.DAYS);
        var eventsEndsAt = faker.date().future(20, java.util.concurrent.TimeUnit.DAYS, eventStartsAt);
        var event = eventRepository.save(Event.builder().title(faker.name().title()).address(faker.address().fullAddress()).price((float) faker.number().randomDouble(1, 1, 1000)).startsAt(eventStartsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).endsAt(eventsEndsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).build());
        var indicator = userRepository.save(User.builder().name(faker.name().fullName()).email(faker.internet().emailAddress()).build());
        var indicationAccessSavingDTO = new IndicationAccessSavingDTO(event.getSlug(), indicator.getId().toString());

        var response = indicationAccessController.save(indicationAccessSavingDTO);
        var responseBody = (ErrorMessageDTO) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(String.format("Indicator with id %s is not subscribed to event with slug %s", indicator.getId(), event.getSlug()), responseBody.message());
    }

    @Test
    void shouldSaveIndicationAccess() {
        var eventStartsAt = faker.date().future(10, java.util.concurrent.TimeUnit.DAYS);
        var eventsEndsAt = faker.date().future(20, java.util.concurrent.TimeUnit.DAYS, eventStartsAt);
        var event = eventRepository.save(Event.builder().title(faker.name().title()).address(faker.address().fullAddress()).price((float) faker.number().randomDouble(1, 1, 1000)).startsAt(eventStartsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).endsAt(eventsEndsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).build());
        var indicator = userRepository.save(User.builder().name(faker.name().fullName()).email(faker.internet().emailAddress()).build());
        subscriptionRepository.save(Subscription.builder().event(event).subscriber(indicator).build());
        var indicationAccessSavingDTO = new IndicationAccessSavingDTO(event.getSlug(), indicator.getId().toString());

        var response = indicationAccessController.save(indicationAccessSavingDTO);
        var responseBody = (IndicationAccess) response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(event.getSlug(), responseBody.getEvent().getSlug());
        assertEquals(indicator.getId().toString(), responseBody.getIndicator().getId().toString());
        assertTrue(indicationAccessRepository.existsById(responseBody.getId()));
    }
}


