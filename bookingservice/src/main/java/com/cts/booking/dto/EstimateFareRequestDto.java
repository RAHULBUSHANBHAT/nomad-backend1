package com.cts.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EstimateFareRequestDto {

    @NotBlank(message = "Vehicle type is required.")
    private String vehicleType;

    @Min(value = -90, message = "Invalid pickup latitude.")
    @Max(value = 90, message = "Invalid pickup latitude.")
    private double pickupLat;

    @Min(value = -180, message = "Invalid pickup longitude.")
    @Max(value = 180, message = "Invalid pickup longitude.")
    private double pickupLng;

    @Min(value = -90, message = "Invalid dropoff latitude.")
    @Max(value = 90, message = "Invalid dropoff latitude.")
    private double dropoffLat;

    @Min(value = -180, message = "Invalid dropoff longitude.")
    @Max(value = 180, message = "Invalid dropoff longitude.")
    private double dropoffLng;
}