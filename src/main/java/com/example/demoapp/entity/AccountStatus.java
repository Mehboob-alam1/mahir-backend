package com.example.demoapp.entity;

/** Account lifecycle for admin moderation (login allowed only when ACTIVE and not blocked). */
public enum AccountStatus {
    ACTIVE,
    ON_HOLD,
    DEACTIVATED
}
