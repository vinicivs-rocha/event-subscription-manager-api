package com.example.eventsubscriptionmanagerapi.exceptions;

public class InvalidEventPrice extends RuntimeException {
    public InvalidEventPrice(String message) {
        super(message);
    }
}
