package com.example.eventsubscriptionmanagerapi.dtos;

public record SubscriptionCreationDTO(String eventSlug, String subscriberName, String subscriberEmail,
                                      String indicatorId) {
}
