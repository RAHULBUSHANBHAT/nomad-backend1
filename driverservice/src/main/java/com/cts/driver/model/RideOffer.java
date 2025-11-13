package com.cts.driver.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "ride_offers")
public class RideOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String bookingId;

    @Column(nullable = false)
    private String driverId; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideOfferStatus status;
    
    @Column(nullable = false)
    private String vehicleCategory;

    @Column(nullable = false)
    private double fare;

    @Column(nullable = false)
    private String pickupLocationName;

    @Column(nullable = false)
    private String dropoffLocationName;

    @Column(nullable = false)
    private double distanceInKm;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = LocalDateTime.now().plusSeconds(45); 
        status = RideOfferStatus.PENDING;
    }

    public RideOffer(String bookingId, String driverId, String vehicleCategory, double fare,
                    String pickupLocationName, String dropoffLocationName, double distanceInKm) {
                        
        this.bookingId = bookingId;
        this.driverId = driverId;
        this.vehicleCategory = vehicleCategory;
        this.fare = fare;
        this.pickupLocationName = pickupLocationName;
        this.dropoffLocationName = dropoffLocationName;
        this.distanceInKm = distanceInKm;
    }
}