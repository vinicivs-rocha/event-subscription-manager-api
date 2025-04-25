package com.example.eventsubscriptionmanagerapi.services;

import com.example.eventsubscriptionmanagerapi.dtos.EventCreationDTO;
import com.example.eventsubscriptionmanagerapi.exceptions.EventNotFoundException;
import com.example.eventsubscriptionmanagerapi.models.Event;
import com.example.eventsubscriptionmanagerapi.repositories.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event create(EventCreationDTO eventCreationDTO) {
        Event event = Event.builder()
                .title(eventCreationDTO.title())
                .address(eventCreationDTO.address())
                .price(eventCreationDTO.price())
                .startsAt(LocalDate.parse(eventCreationDTO.startsAt()))
                .endsAt(LocalDate.parse(eventCreationDTO.endsAt()))
                .build();
        return eventRepository.save(event);
    }

    public List<Event> list() {
        return (List<Event>) eventRepository.findAll();
    }

    public Event detailBySlug(String slug) {
        return eventRepository.findBySlug(slug).orElseThrow(() -> new EventNotFoundException(String.format("Event with slug %s not found", slug)));
    }
}
