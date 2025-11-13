package com.cts.booking.model;

import com.cts.booking.dto.CreateBookingRequestDto;
import com.cts.booking.dto.client.UserDto; // Import UserDto
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
    
    // --- NEW RIDER SNAPSHOT FIELDS ---
    @Column(updatable = false)
    private String riderName;
    
    @Column(updatable = false)
    private String riderPhoneNumber;

    @Column(nullable = true)
    private String driverUserId; // Nullable until a driver accepts

    // --- NEW DRIVER SNAPSHOT FIELDS ---
    @Column
    private String driverName;
    
    @Column
    private String driverPhoneNumber;
    
    /** The driver's OVERALL rating at the time of booking. */
    @Column
    private float driverProfileRating; 
    
    /** The driver's total trips at the time of booking. */
    @Column
    private long totalTrips;
    
    /** The driver's join date. */
    @Column
    private LocalDateTime driverCreatedAt;
    
    // --- END NEW FIELDS ---
    
    @Column(updatable = false)
    private String vehicleId; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    // Location Info
    @Column(nullable = false)
    private String pickupLocationName; 
    @Column(nullable = false)
    private String dropoffLocationName; 
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

    // Financial Info
    @Column
    private double fare; 
    @Column
    private String paymentStatus;

    // Timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestTime;
    @Column
    private LocalDateTime acceptedTime;
    @Column
    private LocalDateTime pickupTime; 
    @Column
    private LocalDateTime completedTime; 
    
    /** The rating GIVEN BY THE RIDER for THIS RIDE (1-5). */
    @Column
    private Integer driverRating; 

    @PrePersist
    protected void onCreate() {
        requestTime = LocalDateTime.now();
        status = BookingStatus.PENDING;
        paymentStatus = "UNPAID";
    }

    /**
     * Helper constructor to create a new PENDING booking from a request.
     * NOW INCLUDES RIDER SNAPSHOT.
     */
    public Booking(String riderUserId, UserDto rider, CreateBookingRequestDto dto, String city, double fare) {
        this.riderUserId = riderUserId;
        
        // Populate rider snapshot
        this.riderName = rider.getFirstName() + " " + rider.getLastName();
        this.riderPhoneNumber = rider.getPhoneNumber();
        
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