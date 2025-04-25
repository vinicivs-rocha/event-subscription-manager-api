package com.example.eventsubscriptionmanagerapi.controllers;

import com.example.eventsubscriptionmanagerapi.dtos.EventCreationDTO;
import com.example.eventsubscriptionmanagerapi.models.Event;
import com.example.eventsubscriptionmanagerapi.services.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventsController {
    private final EventService eventService;

    public EventsController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public Event create(@RequestBody EventCreationDTO eventCreationDTO) {
        return eventService.create(eventCreationDTO);
    }

    @GetMapping
    public List<Event> list() {
        return eventService.list();
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Event> detailBySlug(@PathVariable String slug) {
        Event event =  eventService.detailBySlug(slug);
        return ResponseEntity.ok(event);
    }
}
