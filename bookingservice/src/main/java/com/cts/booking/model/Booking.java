package com.cts.booking.model;

import com.cts.booking.dto.CreateBookingRequestDto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, updatable = false)
    private String riderUserId;

    @Column(nullable = true)
    private String driverUserId; // Nullable until a driver accepts
    
    @Column(updatable = false)
    private String vehicleId; // From the driver's vehicle (from your mock data)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    // Location Info (from your mock data)
    @Column(nullable = false)
    private String pickupLocationName; // Renamed from "pickup_address"
    @Column(nullable = false)
    private String dropoffLocationName; // Renamed from "dropoff_address"
    @Column(nullable = false)
    private double pickupLat;
    @Column(nullable = false)
    private double pickupLng;
    @Column(nullable = false)
    private double dropoffLat;
    @Column(nullable = false)
    private double dropoffLng;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String vehicleType;

    // Financial Info (from your mock data)
    @Column
    private double fare; // Renamed from "fare_amount"
    @Column
    private String paymentStatus;

    // Timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestTime;
    @Column
    private LocalDateTime acceptedTime;
    @Column
    private LocalDateTime pickupTime; // Renamed from "pickup_time"
    @Column
    private LocalDateTime completedTime; // Renamed from "dropoff_time"
    
    // Feedback (New)
    @Column
    private Integer driverRating; // Storing the rating (1-5)

    @PrePersist
    protected void onCreate() {
        requestTime = LocalDateTime.now();
        status = BookingStatus.PENDING;
        paymentStatus = "UNPAID";
    }

    /**
     * Helper constructor to create a new PENDING booking from a request.
     */
    public Booking(String riderUserId, CreateBookingRequestDto dto, String city, double fare) {
        this.riderUserId = riderUserId;
        this.pickupLocationName = dto.getPickupLocationName();
        this.dropoffLocationName = dto.getDropoffLocationName();
        this.pickupLat = dto.getPickupLat();
        this.pickupLng = dto.getPickupLng();
        this.dropoffLat = dto.getDropoffLat();
        this.dropoffLng = dto.getDropoffLng();
        this.city = city;
        this.vehicleType = dto.getVehicleType();
        this.fare = fare;
    }
}