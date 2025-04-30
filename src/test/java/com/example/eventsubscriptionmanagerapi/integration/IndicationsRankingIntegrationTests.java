package com.example.eventsubscriptionmanagerapi.integration;

import com.example.eventsubscriptionmanagerapi.controllers.SubscriptionController;
import com.example.eventsubscriptionmanagerapi.dtos.ErrorMessageDTO;
import com.example.eventsubscriptionmanagerapi.dtos.IndicationRankingItemDTO;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class IndicationsRankingIntegrationTests {
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
        var nonExistentEventSlug = faker.internet().slug();

        var response = subscriptionController.queryIndicationsRanking(nonExistentEventSlug);
        var responseBody = (ErrorMessageDTO) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(String.format("Event with slug %s not found", nonExistentEventSlug), responseBody.message());
    }

    @Test
    void shouldReturnIndicationsRanking() {
        var eventStartsAt = faker.date().future(10, TimeUnit.DAYS);
        var eventEndsAt = faker.date().future(10, TimeUnit.DAYS, eventStartsAt);
        var event = eventRepository.save(Event.builder().title(faker.name().title()).address(faker.address().fullAddress()).price((float) faker.number().randomDouble(1, 1, 1000)).startsAt(eventStartsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).endsAt(eventEndsAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).build());
        var firstSubscriber = userRepository.save(User.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .build());
        subscriptionRepository.save(Subscription.builder().subscriber(firstSubscriber).event(event).build());

        for (int i = 0; i < 3; i++) {
            var subscriber = userRepository.save(User.builder()
                    .name(faker.name().fullName())
                    .email(faker.internet().emailAddress())
                    .build());
            subscriptionRepository.save(Subscription.builder().subscriber(subscriber).event(event).indicator(firstSubscriber).build());
        }

        var secondSubscriber = userRepository.save(User.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .build());
        subscriptionRepository.save(Subscription.builder().subscriber(secondSubscriber).event(event).build());

        for (int i = 0; i < 2; i++) {
            var subscriber = userRepository.save(User.builder()
                    .name(faker.name().fullName())
                    .email(faker.internet().emailAddress())
                    .build());
            subscriptionRepository.save(Subscription.builder().subscriber(subscriber).event(event).indicator(secondSubscriber).build());
        }

        var thirdSubscriber = userRepository.save(User.builder()
                .name(faker.name().fullName())
                .email(faker.internet().emailAddress())
                .build());
        subscriptionRepository.save(Subscription.builder().subscriber(thirdSubscriber).event(event).build());

        for (int i = 0; i < 1; i++) {
            var subscriber = userRepository.save(User.builder()
                    .name(faker.name().fullName())
                    .email(faker.internet().emailAddress())
                    .build());
            subscriptionRepository.save(Subscription.builder().subscriber(subscriber).event(event).indicator(thirdSubscriber).build());
        }

        var ranking = subscriptionController.queryIndicationsRanking(event.getSlug());
        @SuppressWarnings("unchecked") var responseBody = (List<IndicationRankingItemDTO>) ranking.getBody();

        assertEquals(HttpStatus.OK, ranking.getStatusCode());
        assertNotNull(responseBody);
        assertEquals(3, responseBody.size());
        var firstSubscriberIndicationsRankingItemDto = responseBody.getFirst();
        assertEquals(3, firstSubscriberIndicationsRankingItemDto.indicationsCount());
        assertEquals(firstSubscriber.getId(), firstSubscriberIndicationsRankingItemDto.indicatorId());
        assertEquals(firstSubscriber.getName(), firstSubscriberIndicationsRankingItemDto.indicatorName());
        var secondSubscriberIndicationsRankingItemDto = responseBody.get(1);
        assertEquals(2, secondSubscriberIndicationsRankingItemDto.indicationsCount());
        assertEquals(secondSubscriber.getId(), secondSubscriberIndicationsRankingItemDto.indicatorId());
        assertEquals(secondSubscriber.getName(), secondSubscriberIndicationsRankingItemDto.indicatorName());
        var thirdSubscriberIndicationsRankingItemDto = responseBody.getLast();
        assertEquals(1, thirdSubscriberIndicationsRankingItemDto.indicationsCount());
        assertEquals(thirdSubscriber.getId(), thirdSubscriberIndicationsRankingItemDto.indicatorId());
        assertEquals(thirdSubscriber.getName(), thirdSubscriberIndicationsRankingItemDto.indicatorName());
    }
}


