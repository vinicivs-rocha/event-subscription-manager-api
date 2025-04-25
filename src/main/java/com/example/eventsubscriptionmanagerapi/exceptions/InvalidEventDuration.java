package com.example.eventsubscriptionmanagerapi.exceptions;

public class InvalidEventDuration extends RuntimeException {
    public InvalidEventDuration(String message) {
        super(message);
    }
}
