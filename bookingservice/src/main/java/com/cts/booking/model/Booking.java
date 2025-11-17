package com.cts.booking.model;

import com.cts.booking.dto.CreateBookingRequestDto;
import com.cts.booking.dto.client.UserDto;
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
    
    @Column(updatable = false)
    private String riderName;
    
    @Column(updatable = false)
    private String riderPhoneNumber;

    @Column(nullable = true)
    private String driverUserId;

    @Column
    private String driverName;
    
    @Column
    private String driverPhoneNumber;
    
    @Column
    private float driverProfileRating; 
    
    @Column
    private long totalTrips;
    
    @Column
    private LocalDateTime driverCreatedAt;
    
    @Column(updatable = false)
    private String vehicleId; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

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

    @Column
    private double fare; 
    @Column
    private String paymentStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestTime;
    @Column
    private LocalDateTime acceptedTime;
    @Column
    private LocalDateTime pickupTime; 
    @Column
    private LocalDateTime completedTime; 
    
    @Column
    private Integer driverRating; 

    @PrePersist
    protected void onCreate() {
        requestTime = LocalDateTime.now();
        status = BookingStatus.PENDING;
        paymentStatus = "UNPAID";
    }

    public Booking(String riderUserId, UserDto rider, CreateBookingRequestDto dto, String city, double fare) {
        this.riderUserId = riderUserId;
        
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