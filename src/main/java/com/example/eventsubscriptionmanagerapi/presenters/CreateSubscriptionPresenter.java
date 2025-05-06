package com.example.eventsubscriptionmanagerapi.presenters;

import com.example.eventsubscriptionmanagerapi.dtos.SubscriptionPublicDTO;
import com.example.eventsubscriptionmanagerapi.models.Subscription;

public class CreateSubscriptionPresenter {
    private CreateSubscriptionPresenter() {}

    public static SubscriptionPublicDTO toHTTP(Subscription subscription) {
        return new SubscriptionPublicDTO(subscription.getId().toString());
    }
}
