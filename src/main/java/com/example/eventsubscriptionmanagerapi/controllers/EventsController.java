package com.example.eventsubscriptionmanagerapi.controllers;

import com.example.eventsubscriptionmanagerapi.dtos.ErrorMessageDTO;
import com.example.eventsubscriptionmanagerapi.dtos.EventCreationDTO;
import com.example.eventsubscriptionmanagerapi.exceptions.EventAlreadyExistsException;
import com.example.eventsubscriptionmanagerapi.exceptions.EventNotFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.InvalidEventDuration;
import com.example.eventsubscriptionmanagerapi.models.Event;
import com.example.eventsubscriptionmanagerapi.services.EventService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Object> create(@RequestBody EventCreationDTO eventCreationDTO) {
        try {
            return ResponseEntity.ok(eventService.create(eventCreationDTO));
        } catch (EventAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorMessageDTO(e.getMessage()));
        } catch (InvalidEventDuration e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessageDTO(e.getMessage()));
        }
    }

    @GetMapping
    public List<Event> list() {
        return eventService.list();
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Object> detailBySlug(@PathVariable String slug) {
        try {
            return ResponseEntity.ok(eventService.detailBySlug(slug));
        } catch (EventNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessageDTO(e.getMessage()));
        }
    }
}
