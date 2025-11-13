package com.cts.driver.dto;

import com.cts.driver.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleTypeCountDto {
    private VehicleType vehicleType;
    private long availableCount;
}