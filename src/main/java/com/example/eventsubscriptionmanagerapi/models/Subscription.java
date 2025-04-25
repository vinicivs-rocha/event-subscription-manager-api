package com.example.eventsubscriptionmanagerapi.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @Setter
    private Event event;

    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = false)
    @Setter
    private User subscriber;

    @ManyToOne
    @JoinColumn(name = "indicator_id")
    @Setter
    private User indicator;
}
