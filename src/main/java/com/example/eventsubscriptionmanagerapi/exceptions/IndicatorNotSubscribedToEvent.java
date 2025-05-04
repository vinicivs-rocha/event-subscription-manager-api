package com.example.eventsubscriptionmanagerapi.exceptions;

public class IndicatorNotSubscribedToEvent extends RuntimeException {
    public IndicatorNotSubscribedToEvent(String message) {
        super(message);
    }
}
