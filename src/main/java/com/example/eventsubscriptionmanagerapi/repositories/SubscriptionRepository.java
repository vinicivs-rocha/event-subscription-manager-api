package com.example.eventsubscriptionmanagerapi.repositories;

import com.example.eventsubscriptionmanagerapi.dtos.IndicationRankingItemDTO;
import com.example.eventsubscriptionmanagerapi.dtos.IndicatorRankingDTO;
import com.example.eventsubscriptionmanagerapi.models.Event;
import com.example.eventsubscriptionmanagerapi.models.Subscription;
import com.example.eventsubscriptionmanagerapi.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends CrudRepository<Subscription, UUID> {
    Optional<Subscription> findByEventAndSubscriber(Event event, User subscriber);

    @Query(value = """
            select count(subscriptions.id) as "indicationsCount", \
                   indicators.id as "indicatorId", \
                   indicators.name as "indicatorName" \
            from subscriptions \
                     inner join users indicators on subscriptions.indicator_id = indicators.id \
            where subscriptions.event_id = :eventId \
            group by indicators.id \
            order by "indicationsCount" desc \
            limit 3""", nativeQuery = true)
    List<IndicationRankingItemDTO> queryIndicationsRanking(@Param("eventId") UUID eventId);

    @Query(value = """
        with "rankedIndicators" as ( \
            select count(subscriptions.id) as "indicationsCount", \
                   indicators.id as "indicatorId", \
                   indicators.name as "indicatorName", \
                   row_number() over (order by count(subscriptions.id) desc) as "indicatorRanking" \
            from subscriptions \
                     inner join users indicators on subscriptions.indicator_id = indicators.id \
            where subscriptions.event_id = :eventId \
            group by indicators.id \
        ) \
        select "indicationsCount", "indicatorId", "indicatorName", "indicatorRanking" \
        from "rankedIndicators" \
        where "indicatorId" = :indicatorId \
        """, nativeQuery = true)
    Optional<IndicatorRankingDTO> queryIndicatorRanking(@Param("eventId") UUID eventId, @Param("indicatorId") UUID indicatorId);
}
