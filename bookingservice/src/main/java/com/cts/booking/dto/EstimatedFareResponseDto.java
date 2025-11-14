package com.cts.booking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstimatedFareResponseDto {
    private String city;
    private String vehicleType;
    private double distanceInKm;
    private double baseFare;
    private double distanceFare;
    private double totalFare;
}