package com.cts.booking.dto;

import com.cts.booking.model.BookingStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingDto {
    private String id;
    private BookingStatus status;

    // --- ENRICHED DETAILS (Fetched from User-Service) ---
    private String riderUserId;
    private String riderName;
    private String riderPhoneNumber;

    private String driverUserId;
    private String driverName;
    private String driverPhoneNumber;
    private Double driverRating;
    
    // --- VEHICLE DETAILS ---
    private String vehicleId;   // Specific vehicle used
    private String vehicleType; // "Sedan"
    
    // --- RIDE INFO ---
    private String pickupLocationName;
    private String dropoffLocationName;
    private double pickupLat;
    private double pickupLng;
    private double dropoffLat;
    private double dropoffLng;
    private String city;
    
    // --- FINANCIAL ---
    private double fare;
    private String paymentStatus; // "UNPAID", "PAID", "PAID_CASH"

    // --- TIMESTAMPS ---
    private LocalDateTime requestTime;
    private LocalDateTime acceptedTime;
    private LocalDateTime pickupTime;
    private LocalDateTime completedTime;
}