package com.cts.driver.dto;

import java.time.LocalDate;

import com.cts.driver.model.VehicleType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VehicleDto {

    private String id;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "Registration number is required")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,2}[0-9]{4}$", message = "Invalid Vehicle Registration Number format")
    private String registrationNumber;

    @NotBlank(message = "PUC Number is required")
    private String manufacturer;

    @NotBlank(message = "Model is required")
    @Size(min = 2, max = 50, message = "Model name must be between 2 and 50 characters")
    private String model;
    
    @NotBlank(message = "PUC Number is required")
    private String color;

    @NotNull(message = "PUC Expiry date is required")
    @Future(message = "PUC Expiry must be a future date")
    private LocalDate pucExpiry;

    @NotBlank(message = "Insurance Policy Number is required")
    @Size(min = 8, max = 30, message = "Policy number length is invalid")
    private String insurancePolicyNumber;

    @NotNull(message = "Insurance Expiry date is required")
    @Future(message = "Insurance Expiry must be a future date")
    private LocalDate insuranceExpiry;

    @NotBlank(message = "PUC Number is required")
    private String pucNumber;

    private boolean isPucVerified;
    private boolean isInsuranceVerified;
    private boolean isRcVerified;
}