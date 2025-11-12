package com.cts.booking.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is the message we send to start the "Simple Matcher"
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestEventDto {
    private String bookingId;
    private String city;
    private String vehicleType;
}