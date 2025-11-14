package com.cts.driver.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestEventDto {
    private String bookingId;
    private String city;
    private String vehicleType;
    private double fare;
    private String pickupLocationName;
    private String dropoffLocationName;
    private double estimatedDistanceKm;
}