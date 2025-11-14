package com.cts.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverStatsDto {
    private long driverCount;
    private long vehicleCount;
}
