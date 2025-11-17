package com.cts.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateDriverStatusDto {
    @NotBlank(message = "Current city is required to go online")
    private String currentCity;
    private boolean isAvailable;
}