package com.cts.driver.dto.kafka;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// This DTO must match the one from the (future) booking-service
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestEventDto {
    private String bookingId;
    private String city;
    private String vehicleType;
    private double fare;
}