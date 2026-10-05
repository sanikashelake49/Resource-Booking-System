package com.example.resourcebooking.exception;

/**
 * Thrown when a new/updated reservation overlaps with an existing
 * active (PENDING/CONFIRMED) reservation for the same resource.
 * Maps to HTTP 409 CONFLICT.
 */
public class BookingConflictException extends RuntimeException {
    public BookingConflictException(String message) {
        super(message);
    }
}
