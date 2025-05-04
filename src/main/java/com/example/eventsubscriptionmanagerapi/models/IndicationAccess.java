package com.example.eventsubscriptionmanagerapi.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "indication_accesses")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class IndicationAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @Setter
    private Event event;

    @ManyToOne
    @JoinColumn(name = "indicator_id", nullable = false)
    @Setter
    private User indicator;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    @Setter
    private Subscription subscription;
}
