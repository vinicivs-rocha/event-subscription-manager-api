package com.example.eventsubscriptionmanagerapi.repositories;

import com.example.eventsubscriptionmanagerapi.models.Event;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends CrudRepository<Event, UUID> {
    Optional<Event> findBySlug(String slug);

    boolean existsByTitle(String title);
}
