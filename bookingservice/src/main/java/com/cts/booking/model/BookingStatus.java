package com.cts.booking.model;

public enum BookingStatus {
    PENDING,            // Rider requested
    ACCEPTED,           // Driver assigned
    IN_PROGRESS,        // Ride started
    AWAITING_PAYMENT,   // Ride physically ended, payment pending <-- NEW
    PAID,               // Payment successful, feedback pending
    COMPLETED,          // Feedback given (or skipped), fully closed
    CANCELLED           // Cancelled by user/driver/admin
}