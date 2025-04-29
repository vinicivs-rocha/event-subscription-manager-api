package com.example.eventsubscriptionmanagerapi.integration;

import com.example.eventsubscriptionmanagerapi.controllers.SubscriptionController;
import com.example.eventsubscriptionmanagerapi.dtos.ErrorMessageDTO;
import com.example.eventsubscriptionmanagerapi.dtos.SubscriptionCreationDTO;
import com.example.eventsubscriptionmanagerapi.dtos.SubscriptionPublicDTO;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class SubscriptionCreationIntegrationTests {
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
    void shouldThrowEventNotFoundException_whenEventSlugDoesNotExist() {
        var nonExistentEventSlug = Event.builder().title(faker.name().title()).build().getSlug();
        var subscriptionCreationDTO = new SubscriptionCreationDTO(nonExistentEventSlug, faker.name().fullName(), faker.internet().emailAddress(), null);

        var response = subscriptionController.create(subscriptionCreationDTO);
        var responseBody = (ErrorMessageDTO) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(String.format("Event with slug %s not found", nonExistentEventSlug), responseBody.message());
    }

    @Test
    void shouldThrowIndicatorNotFoundException_whenIndicatorIdDoesNotExist() {
        var nonExistentIndicatorId = UUID.randomUUID().toString();
        var eventStartsAt = faker.date().future(10, java.util.concurrent.TimeUnit.DAYS);
        var eventsEndsAt = faker.date().future(20, java.util.concurrent.TimeUnit.DAYS, eventStartsAt);
        var event = eventRepository.save(Event.builder().title(faker.name().title()).address(faker.address().fullAddress()).price((float) faker.number().randomDouble(1, 1, 1000)).startsAt(eventStartsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).endsAt(eventsEndsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).build());
        var subscriptionCreationDTO = new SubscriptionCreationDTO(event.getSlug(), faker.name().fullName(), faker.internet().emailAddress(), nonExistentIndicatorId);

        var response = subscriptionController.create(subscriptionCreationDTO);
        var responseBody = (ErrorMessageDTO) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(String.format("Indicator with id %s not found", nonExistentIndicatorId), responseBody.message());
    }

    @Test
    void shouldThrowSubscriptionAlreadyExistsException_whenSubscriberIsAlreadySubscribedInTheEvent() {
        var eventStartsAt = faker.date().future(10, java.util.concurrent.TimeUnit.DAYS);
        var eventsEndsAt = faker.date().future(20, java.util.concurrent.TimeUnit.DAYS, eventStartsAt);
        var event = eventRepository.save(Event.builder().title(faker.name().title()).address(faker.address().fullAddress()).price((float) faker.number().randomDouble(1, 1, 1000)).startsAt(eventStartsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).endsAt(eventsEndsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).build());
        var subscriber = userRepository.save(User.builder().name(faker.name().fullName()).email(faker.internet().emailAddress()).build());
        subscriptionRepository.save(Subscription.builder().event(event).subscriber(subscriber).build());
        var subscriptionCreationDTO = new SubscriptionCreationDTO(event.getSlug(), subscriber.getName(), subscriber.getEmail(), null);

        var response = subscriptionController.create(subscriptionCreationDTO);
        var responseBody = (ErrorMessageDTO) response.getBody();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(String.format("User %s already subscribed to %s", subscriber.getName(), event.getTitle()), responseBody.message());
    }

    @Test
    void shouldCreateSubscriber_whenCreatingSubscriptionWithNonExistingSubscriber() {
        var eventStartsAt = faker.date().future(10, java.util.concurrent.TimeUnit.DAYS);
        var eventsEndsAt = faker.date().future(20, java.util.concurrent.TimeUnit.DAYS, eventStartsAt);
        var event = eventRepository.save(Event.builder().title(faker.name().title()).address(faker.address().fullAddress()).price((float) faker.number().randomDouble(1, 1, 1000)).startsAt(eventStartsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).endsAt(eventsEndsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).build());
        var subscriptionCreationDTO = new SubscriptionCreationDTO(event.getSlug(), faker.name().fullName(), faker.internet().emailAddress(), null);
        var didSubscriberExistBeforeSubscriptionCreation = userRepository.existsByEmail(subscriptionCreationDTO.subscriberEmail());

        var response = subscriptionController.create(subscriptionCreationDTO);
        var responseBody = (SubscriptionPublicDTO) response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(responseBody);
        assertFalse(didSubscriberExistBeforeSubscriptionCreation);
        assertTrue(userRepository.existsByEmail(subscriptionCreationDTO.subscriberEmail()));
    }

    @Test
    void shouldUseAlreadyExistingSubscriber_whenCreatingSubscriptionWithExistingSubscriber() {
        var eventStartsAt = faker.date().future(10, java.util.concurrent.TimeUnit.DAYS);
        var eventsEndsAt = faker.date().future(20, java.util.concurrent.TimeUnit.DAYS, eventStartsAt);
        var event = eventRepository.save(Event.builder().title(faker.name().title()).address(faker.address().fullAddress()).price((float) faker.number().randomDouble(1, 1, 1000)).startsAt(eventStartsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).endsAt(eventsEndsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).build());
        var subscriber = userRepository.save(User.builder().name(faker.name().fullName()).email(faker.internet().emailAddress()).build());
        var subscriptionCreationDTO = new SubscriptionCreationDTO(event.getSlug(), subscriber.getName(), subscriber.getEmail(), null);

        var response = subscriptionController.create(subscriptionCreationDTO);
        var responseBody = (SubscriptionPublicDTO) response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(subscriber.getId(), subscriptionRepository.findById(UUID.fromString(responseBody.id())).orElseThrow().getSubscriber().getId());
    }

    @Test
    void shouldUseEventSlugAndSubscriberIdToFillDesignation_whenCreatingValidSubscription() {
        var eventStartsAt = faker.date().future(10, java.util.concurrent.TimeUnit.DAYS);
        var eventsEndsAt = faker.date().future(20, java.util.concurrent.TimeUnit.DAYS, eventStartsAt);
        var event = eventRepository.save(Event.builder().title(faker.name().title()).address(faker.address().fullAddress()).price((float) faker.number().randomDouble(1, 1, 1000)).startsAt(eventStartsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).endsAt(eventsEndsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).build());
        var subscriber = userRepository.save(User.builder().name(faker.name().fullName()).email(faker.internet().emailAddress()).build());
        var subscriptionCreationDTO = new SubscriptionCreationDTO(event.getSlug(), subscriber.getName(), subscriber.getEmail(), null);

        var response = subscriptionController.create(subscriptionCreationDTO);
        var responseBody = (SubscriptionPublicDTO) response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(String.format("http://codecraft.com/%s/%s", event.getSlug(), subscriber.getId()), responseBody.designation());
    }
}


