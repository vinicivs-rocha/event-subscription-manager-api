package com.example.eventsubscriptionmanagerapi;

import com.example.eventsubscriptionmanagerapi.dtos.SubscriptionPublicDTO;
import com.example.eventsubscriptionmanagerapi.models.Subscription;

public class CreateSubscriptionPresenter {
    private CreateSubscriptionPresenter() {}

    public static SubscriptionPublicDTO toHTTP(Subscription subscription) {
        return new SubscriptionPublicDTO(subscription.getId().toString(), String.format("http://codecraft.com/%s/%s", subscription.getEvent().getSlug(), subscription.getSubscriber().getId()));
    }
}
