package com.cts.driver.dto;

import java.time.LocalDate;

import com.cts.driver.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleDto {
    private String id;
    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;
    @NotBlank(message = "Registration number is required")
    private String registrationNumber;
    @NotBlank(message = "Model is required")
    private String model;
    
    private String rcNumber;
    private LocalDate pucExpiry;
    private String insurancePolicyNumber;
    private LocalDate insuranceExpiry;
    private String pucNumber;
    private boolean isPucVerified;
    private boolean isInsuranceVerified;
}