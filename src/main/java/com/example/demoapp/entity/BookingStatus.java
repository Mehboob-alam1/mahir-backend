package com.example.demoapp.entity;

public enum BookingStatus {
    PENDING,      // Legacy / not used when created from bid
    ACCEPTED,     // Bid accepted, Mahir hired
    REACHED,      // User (job poster) marks: Mahir has reached / agreed via chat
    IN_PROGRESS,  // Working – job in progress (User updates)
    COMPLETED,    // Done (User updates); then User can review Mahir
    CANCELLED     // Cancelled by either party
}
