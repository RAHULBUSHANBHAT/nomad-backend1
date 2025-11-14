package com.cts.driver.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RideOfferDto {

    private String offerId;
    private String bookingId;
    private double fare;
    private String vehicleCategory;
    private LocalDateTime expiresAt;
    private String pickupLocationName;
    private String dropoffLocationName;
    private double estimatedDistanceKm;
    private LocalDateTime createdAt;
}