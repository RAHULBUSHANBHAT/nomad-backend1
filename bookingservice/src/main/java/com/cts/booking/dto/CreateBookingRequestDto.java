package com.cts.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBookingRequestDto {
    @NotBlank
    private String pickupLocationName;
    @NotBlank
    private String dropoffLocationName;
    @NotNull
    private double pickupLat;
    @NotNull
    private double pickupLng;
    @NotNull
    private double dropoffLat;
    @NotNull
    private double dropoffLng;
    @NotBlank
    private String city;
    @NotBlank
    private String vehicleType;
}