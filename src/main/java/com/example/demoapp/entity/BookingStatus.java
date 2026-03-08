package com.example.demoapp.entity;

public enum BookingStatus {
    PENDING,    // USER requested, waiting for MAHIR
    ACCEPTED,   // MAHIR accepted
    REJECTED,   // MAHIR rejected
    COMPLETED,  // Service done
    CANCELLED   // Cancelled by either party
}
