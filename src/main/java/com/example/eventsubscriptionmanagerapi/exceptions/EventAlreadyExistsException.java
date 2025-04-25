package com.example.eventsubscriptionmanagerapi.exceptions;

public class EventAlreadyExistsException extends RuntimeException {
    public EventAlreadyExistsException(String message) { super(message); }
}
