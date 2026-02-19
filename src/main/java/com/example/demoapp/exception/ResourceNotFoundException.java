package com.example.demoapp.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s non trovato con id: %d", resourceName, id));
    }
}
