package com.example.eventsubscriptionmanagerapi.services;

import com.example.eventsubscriptionmanagerapi.dtos.IndicationRankingItemDTO;
import com.example.eventsubscriptionmanagerapi.dtos.IndicatorRankingDTO;
import com.example.eventsubscriptionmanagerapi.dtos.SubscriptionCreationDTO;
import com.example.eventsubscriptionmanagerapi.exceptions.EventNotFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.IndicatorNotFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.NoIndicationFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.SubscriptionAlreadyExistsException;
import com.example.eventsubscriptionmanagerapi.models.Event;
import com.example.eventsubscriptionmanagerapi.models.Subscription;
import com.example.eventsubscriptionmanagerapi.models.User;
import com.example.eventsubscriptionmanagerapi.repositories.EventRepository;
import com.example.eventsubscriptionmanagerapi.repositories.SubscriptionRepository;
import com.example.eventsubscriptionmanagerapi.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public Subscription create(SubscriptionCreationDTO subscriptionCreationDTO) {
        Event event = eventRepository.findBySlug(subscriptionCreationDTO.eventSlug()).orElseThrow(() -> new EventNotFoundException(String.format("Event with slug %s not found", subscriptionCreationDTO.eventSlug())));
        User subscriber = userRepository.findByEmail(subscriptionCreationDTO.subscriberEmail()).orElseGet(() -> {
            User newSubscriber = User.builder().name(subscriptionCreationDTO.subscriberName()).email(subscriptionCreationDTO.subscriberEmail()).build();
            return userRepository.save(newSubscriber);
        });
        subscriptionRepository.findByEventAndSubscriber(event, subscriber).ifPresent(s -> {
            throw new SubscriptionAlreadyExistsException(String.format("User %s already subscribed to %s", subscriber.getName(), event.getTitle()));
        });
        String indicatorId = subscriptionCreationDTO.indicatorId();
        User indicator = indicatorId != null ? userRepository.findById(UUID.fromString(subscriptionCreationDTO.indicatorId())).orElseThrow(() -> new IndicatorNotFoundException(String.format("Indicator with id %s not found", subscriptionCreationDTO.indicatorId()))) : null;
        return subscriptionRepository.save(Subscription.builder().event(event).subscriber(subscriber).indicator(indicator).build());
    }

    public List<IndicationRankingItemDTO> queryIndicationsRanking(String eventSlug) {
        Event event = eventRepository.findBySlug(eventSlug).orElseThrow(() -> new EventNotFoundException(String.format("Event with slug %s not found", eventSlug)));
        return subscriptionRepository.queryIndicationsRanking(event.getId());
    }

    public IndicatorRankingDTO queryIndicatorRanking(String eventSlug, UUID indicatorId) {
        Event event = eventRepository.findBySlug(eventSlug).orElseThrow(() -> new EventNotFoundException(String.format("Event with slug %s not found", eventSlug)));
        if (!userRepository.existsById(indicatorId)) {
            throw new IndicatorNotFoundException(String.format("Indicator with id %s not found", indicatorId));
        }
        return subscriptionRepository.queryIndicatorRanking(event.getId(), indicatorId).orElseThrow(() -> new NoIndicationFoundException(String.format("User with id %s has no indication on the event with slug %s", indicatorId, eventSlug)));
    }
}
