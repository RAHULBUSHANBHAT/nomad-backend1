package com.cts.booking.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingAssignmentDto {
    private String driverUserId;
    private String vehicleId; // <-- This field was missing!
}