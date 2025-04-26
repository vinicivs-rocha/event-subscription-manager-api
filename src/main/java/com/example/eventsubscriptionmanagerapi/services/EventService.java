package com.example.eventsubscriptionmanagerapi.services;

import com.example.eventsubscriptionmanagerapi.dtos.EventCreationDTO;
import com.example.eventsubscriptionmanagerapi.exceptions.EventAlreadyExistsException;
import com.example.eventsubscriptionmanagerapi.exceptions.EventNotFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.InvalidEventDuration;
import com.example.eventsubscriptionmanagerapi.exceptions.InvalidEventPrice;
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
        if (eventRepository.existsByTitle(eventCreationDTO.title())) {
            throw new EventAlreadyExistsException(String.format("Event with title %s already exists", eventCreationDTO.title()));
        }
        if (eventCreationDTO.price() <= 0) {
            throw new InvalidEventPrice("Event price must be greater than 0");
        }
        Event event = Event.builder()
                .title(eventCreationDTO.title())
                .address(eventCreationDTO.address())
                .price(eventCreationDTO.price())
                .startsAt(LocalDate.parse(eventCreationDTO.startsAt()))
                .endsAt(LocalDate.parse(eventCreationDTO.endsAt()))
                .build();
        if (event.getStartsAt().isAfter(event.getEndsAt())) {
            throw new InvalidEventDuration("Event start date cannot be after end date");
        }
        return eventRepository.save(event);
    }

    public List<Event> list() {
        return (List<Event>) eventRepository.findAll();
    }

    public Event detailBySlug(String slug) {
        return eventRepository.findBySlug(slug).orElseThrow(() -> new EventNotFoundException(String.format("Event with slug %s not found", slug)));
    }
}
