package com.example.eventsubscriptionmanagerapi.integration;

import com.example.eventsubscriptionmanagerapi.controllers.SubscriptionController;
import com.example.eventsubscriptionmanagerapi.dtos.ErrorMessageDTO;
import com.example.eventsubscriptionmanagerapi.models.Event;
import com.example.eventsubscriptionmanagerapi.models.Subscription;
import com.example.eventsubscriptionmanagerapi.models.User;
import com.example.eventsubscriptionmanagerapi.repositories.EventRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class SubscriptionDetailingByIdIntegrationTests {
    @Container
    static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest");
    private final Faker faker = new Faker();
    @Autowired
    private SubscriptionController subscriptionController;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;


    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresqlContainer::getDriverClassName);
    }

    @AfterEach
    void tearDown() {
        subscriptionRepository.deleteAll();
    }

    @Test
    void shouldThrowEventNotFound_whenEventDoesNotExist() {
        var nonExistingSubscriptionId = UUID.randomUUID();

        var response = subscriptionController.detailById(nonExistingSubscriptionId);
        var responseBody = (ErrorMessageDTO) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(String.format("Subscription with id %s not found", nonExistingSubscriptionId), responseBody.message());
    }

    @Test
    void shouldDetailSubscription() {
        var eventStartsAt = faker.date().future(10, java.util.concurrent.TimeUnit.DAYS);
        var eventsEndsAt = faker.date().future(20, java.util.concurrent.TimeUnit.DAYS, eventStartsAt);
        var event = eventRepository.save(Event.builder().title(faker.name().title()).address(faker.address().fullAddress()).price((float) faker.number().randomDouble(1, 1, 1000)).startsAt(eventStartsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).endsAt(eventsEndsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).build());
        var subscriber = userRepository.save(User.builder().name(faker.name().fullName()).email(faker.internet().emailAddress()).build());
        var subscription = subscriptionRepository.save(Subscription.builder().event(event).subscriber(subscriber).build());

        var response = subscriptionController.detailById(subscription.getId());
        var responseBody = (Subscription) response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(subscription.getId(), responseBody.getId());
        assertEquals(event.getId(), responseBody.getEvent().getId());
        assertEquals(subscriber.getId(), responseBody.getSubscriber().getId());
    }
}


