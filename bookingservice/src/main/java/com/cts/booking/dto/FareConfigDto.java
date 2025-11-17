package com.cts.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FareConfigDto {
    private String id;
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @NotBlank
    private String vehicleType;
    @Positive
    private double baseFare;
    @Positive
    private double ratePerKm;
}