package com.cts.driver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcceptOfferRequestDto {
    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;
}