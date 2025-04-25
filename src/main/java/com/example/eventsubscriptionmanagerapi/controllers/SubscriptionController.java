package com.example.eventsubscriptionmanagerapi.controllers;

import com.example.eventsubscriptionmanagerapi.CreateSubscriptionPresenter;
import com.example.eventsubscriptionmanagerapi.dtos.ErrorMessageDTO;
import com.example.eventsubscriptionmanagerapi.dtos.SubscriptionCreationDTO;
import com.example.eventsubscriptionmanagerapi.exceptions.EventNotFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.IndicatorNotFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.NoIndicationFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.SubscriptionAlreadyExistsException;
import com.example.eventsubscriptionmanagerapi.models.Subscription;
import com.example.eventsubscriptionmanagerapi.services.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody SubscriptionCreationDTO subscriptionCreationDTO) {
        try {
            Subscription subscription = subscriptionService.create(subscriptionCreationDTO);
            return ResponseEntity.ok(CreateSubscriptionPresenter.toHTTP(subscription));
        } catch (EventNotFoundException | IndicatorNotFoundException e) {
            return ResponseEntity.badRequest().body(new ErrorMessageDTO(e.getMessage()));
        } catch (SubscriptionAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorMessageDTO(e.getMessage()));
        }
    }

    @GetMapping("/indicationsRanking/{eventSlug}")
    public ResponseEntity<Object> queryIndicationsRanking(@PathVariable String eventSlug) {
        try {
            return ResponseEntity.ok(subscriptionService.queryIndicationsRanking(eventSlug));
        } catch (EventNotFoundException e) {
            return ResponseEntity.badRequest().body(new ErrorMessageDTO(e.getMessage()));
        }
    }

    @GetMapping("/indicatorRanking/{eventSlug}/{indicatorId}")
    public ResponseEntity<Object> queryIndicatorRanking(@PathVariable String eventSlug, @PathVariable UUID indicatorId) {
        try {
            return ResponseEntity.ok(subscriptionService.queryIndicatorRanking(eventSlug, indicatorId));
        } catch (EventNotFoundException | IndicatorNotFoundException e) {
            return ResponseEntity.badRequest().body(new ErrorMessageDTO(e.getMessage()));
        } catch (NoIndicationFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessageDTO(e.getMessage()));
        }
    }

}
