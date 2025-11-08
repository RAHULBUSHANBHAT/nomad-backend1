package com.cts.driver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateDriverStatusDto {
    private boolean available;
    @NotBlank(message = "Current city is required to go online")
    private String currentCity;
}