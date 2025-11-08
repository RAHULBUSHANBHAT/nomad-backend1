package com.cts.driver.model;

public enum RideOfferStatus {
    PENDING,  // Sent to driver, no response yet
    ACCEPTED, // Driver accepted
    REJECTED, // Driver rejected
    EXPIRED   // Driver didn't answer in time
}