package com.cts.driver.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingAssignmentDto {
    private String driverUserId;
    private String vehicleId;
}