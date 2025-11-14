package com.cts.driver.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateVerificationDto {

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Aadhaar number must be exactly 12 digits and cannot start with 0 or 1")
    private String aadharNumber;

    @NotBlank(message = "License number is required")
    @Size(min = 10, max = 20, message = "License number must be between 10 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9\\-\\s]+$", message = "License number contains invalid characters")
    private String licenseNumber;

    @Future(message = "License expiry date must be in the future")
    private LocalDate driverLicenseExpiry;
}