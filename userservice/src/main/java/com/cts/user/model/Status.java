package com.cts.user.model;

public enum Status {
    PENDING, // Just registered, needs to verify email or something
    ACTIVE,  // Verified and active
    INACTIVE, // Deactivated by user
    SUSPENDED // Banned by admin
}