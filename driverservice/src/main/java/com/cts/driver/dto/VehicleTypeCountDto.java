package com.cts.driver.dto;

import com.cts.driver.model.VehicleType;

public interface VehicleTypeCountDto {
    VehicleType getVehicleType();
    long getAvailableCount();
}