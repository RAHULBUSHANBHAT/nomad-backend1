package com.cts.driver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ride_offers")
public class RideOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, updatable = false)
    private String bookingId;

    @Column(nullable = false, updatable = false)
    private String driverId; // This is the Driver Entity ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RideOfferStatus status = RideOfferStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public RideOffer(String bookingId, String driverId) {
        this.bookingId = bookingId;
        this.driverId = driverId;
    }
}