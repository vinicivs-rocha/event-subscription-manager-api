package com.example.eventsubscriptionmanagerapi.repositories;

import com.example.eventsubscriptionmanagerapi.models.Event;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends CrudRepository<Event, UUID> {
    public Optional<Event> findBySlug(String slug);
}
