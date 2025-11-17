package com.cts.driver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDriverStatusDto {
    @NotBlank(message = "Current city is required to go online")
    private String currentCity;
    private boolean isAvailable;
}