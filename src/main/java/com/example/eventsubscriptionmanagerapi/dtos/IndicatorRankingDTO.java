package com.example.eventsubscriptionmanagerapi.dtos;

import java.util.UUID;

public record IndicatorRankingDTO(Long indicationsCount, UUID indicatorId, String indicatorName,
                                  Long indicatorRanking) {
}
