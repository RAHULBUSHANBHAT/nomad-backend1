package com.cts.booking.dto;

import com.cts.booking.model.BookingStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingDto {
    private String id;
    private BookingStatus status;

    private String riderUserId;
    private String riderName;
    private String riderPhoneNumber;

    private String driverUserId;
    private String driverName;
    private String driverPhoneNumber;
    private float driverRating;
    private long totalTrips;
    private LocalDateTime driverCreatedAt;
    
    private String vehicleId;
    private String vehicleType;
    
    private String pickupLocationName;
    private String dropoffLocationName;
    private double pickupLat;
    private double pickupLng;
    private double dropoffLat;
    private double dropoffLng;
    private String city;
    
    private double fare;
    private String paymentStatus;

    private LocalDateTime requestTime;
    private LocalDateTime acceptedTime;
    private LocalDateTime pickupTime;
    private LocalDateTime completedTime;
}