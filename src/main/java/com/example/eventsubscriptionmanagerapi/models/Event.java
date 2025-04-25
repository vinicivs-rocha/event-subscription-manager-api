package com.example.eventsubscriptionmanagerapi.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "events")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String title;

    @Column(length = 50, nullable = false)
    private String slug;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Float price;

    @Column(nullable = false)
    private LocalDate startsAt;

    @Column(nullable = false)
    private LocalDate endsAt;

    @PrePersist
    private void fillSlug() {
        if (slug != null)
            return;

        this.slug = generateSlug();
    }

    private String generateSlug() {
        return title.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
