package com.cts.booking.model;

public enum BookingStatus {
    PENDING,    // Rider has requested
    ACCEPTED,   // Driver has accepted
    IN_PROGRESS, // Ride has started
    COMPLETED,  // Ride is finished, payment pending
    PAID,       // Payment is confirmed
    CANCELLED   // <-- NEW status for "Cancel Ride"
}