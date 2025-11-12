package com.cts.booking.dto;

import com.cts.booking.model.BookingStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

// This is the "safe" object we return from our API
@Data
@Builder
public class BookingDto {
    private String id;
    private String riderUserId;
    private String driverUserId;
    private BookingStatus status;
    private String pickupLocationName;
    private String dropoffLocationName;
    private double fare;
    private String paymentStatus;
    private LocalDateTime requestTime;
    private LocalDateTime completedTime;
    private Integer driverRating;
}