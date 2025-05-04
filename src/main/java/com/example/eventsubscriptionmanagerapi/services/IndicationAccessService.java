package com.example.eventsubscriptionmanagerapi.services;

import com.example.eventsubscriptionmanagerapi.dtos.IndicationAccessSavingDTO;
import com.example.eventsubscriptionmanagerapi.exceptions.EventNotFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.IndicatorNotFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.IndicatorNotSubscribedToEvent;
import com.example.eventsubscriptionmanagerapi.models.IndicationAccess;
import com.example.eventsubscriptionmanagerapi.repositories.EventRepository;
import com.example.eventsubscriptionmanagerapi.repositories.IndicationAccessRepository;
import com.example.eventsubscriptionmanagerapi.repositories.SubscriptionRepository;
import com.example.eventsubscriptionmanagerapi.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IndicationAccessService {
    private final IndicationAccessRepository indicationAccessRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;

    public IndicationAccessService(IndicationAccessRepository indicationAccessRepository, UserRepository userRepository, EventRepository eventRepository, SubscriptionRepository subscriptionRepository) {
        this.indicationAccessRepository = indicationAccessRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public IndicationAccess save(IndicationAccessSavingDTO indicationAccessSavingDTO) {
        var indicator = userRepository.findById(UUID.fromString(indicationAccessSavingDTO.indicatorId())).orElseThrow(() -> new IndicatorNotFoundException(String.format("Indicator with id %s not found", indicationAccessSavingDTO.indicatorId())));
        var event = eventRepository.findBySlug(indicationAccessSavingDTO.eventSlug()).orElseThrow(() -> new EventNotFoundException(String.format("Event with slug %s not found", indicationAccessSavingDTO.eventSlug())));
        var subscription = subscriptionRepository.findByEventAndSubscriber(event, indicator).orElseThrow(() -> new IndicatorNotSubscribedToEvent(String.format("Indicator with id %s is not subscribed to event with slug %s", indicationAccessSavingDTO.indicatorId(), indicationAccessSavingDTO.eventSlug())));
        return indicationAccessRepository.save(IndicationAccess.builder().event(event).indicator(indicator).subscription(subscription).build());
    }
}
