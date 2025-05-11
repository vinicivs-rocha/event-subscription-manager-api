package com.example.eventsubscriptionmanagerapi.dtos;

public record EventCreationDTO(String title, String address, Float price, String startsAt, String endsAt,
                               String advertisingContent) {
}
