package com.example.demoapp.exception;

/**
 * Application-level 415 (distinct from Spring's {@code HttpMediaTypeNotSupportedException} for JSON/XML).
 */
public class UnsupportedMediaTypeAppException extends RuntimeException {

    public UnsupportedMediaTypeAppException(String message) {
        super(message);
    }
}
