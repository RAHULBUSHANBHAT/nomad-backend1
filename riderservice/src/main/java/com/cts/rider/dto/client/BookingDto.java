package com.cts.rider.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingDto {
    private String id;
    private String driverId;
    private String riderId;
    private String status;
    private String pickupLocation;
    private String dropoffLocation;
    private double fare;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}