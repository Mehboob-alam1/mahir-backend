package com.example.demoapp.entity;

public enum BookingStatus {
    PENDING,      // Legacy / not used when created from bid
    ACCEPTED,     // Bid accepted, booking confirmed
    IN_PROGRESS,  // Mahir started the job
    COMPLETED,    // Done
    CANCELLED     // Cancelled by either party
}
