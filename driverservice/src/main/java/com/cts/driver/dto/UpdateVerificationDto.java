package com.cts.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Future;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateVerificationDto {
    @NotBlank(message = "Aadhaar number is required")
    private String aadharNumber;
    @NotBlank(message = "PAN number is required")
    private String panNumber;
    @NotBlank(message = "License number is required")
    private String licenseNumber;
    @Future(message = "License expiry date must be in the future")
    private LocalDate driverLicenseExpiry;
}