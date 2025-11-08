package com.cts.driver.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO to send to booking-service
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingAssignmentDto {
    private String driverUserId;
}