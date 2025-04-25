package com.example.eventsubscriptionmanagerapi.dtos;

import java.util.UUID;

public record IndicationRankingItemDTO(Long indicationsCount, UUID indicatorId, String indicatorName) {
}
