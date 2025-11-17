package com.cts.wallet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "ride_transactions")
public class RideTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "booking_id", unique = true, nullable = false)
    private String bookingId;

    @Column(name = "rider_id", nullable = false)
    private String riderId;
    
    @Column(name = "rider_name")
    private String riderName;
    
    @Column(name = "rider_phone")
    private String riderPhone;

    @Column(name = "driver_id", nullable = false)
    private String driverId;
    
    @Column(name = "driver_name")
    private String driverName;
    
    @Column(name = "driver_phone")
    private String driverPhone;

    @Column(name = "base_fare")
    private double baseFare;

    @Column(name = "distance_fare")
    private double distanceFare;

    @Column(name = "taxes")
    private double taxes;
    
    @Column(name = "commission_fee")
    private double commissionFee;

    @Column(name = "total_fare", nullable = false)
    private double totalFare;

    @Column(name = "pickup_address", length = 512)
    private String pickupAddress;

    @Column(name = "dropoff_address", length = 512)
    private String dropoffAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false)
    private PaymentMode paymentMode;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public RideTransaction(String bookingId, String riderId, String riderName, String riderPhone,
            String driverId, String driverName, String driverPhone, double baseFare, double distanceFare, double taxes,
            double commissionFee, double totalFare, String pickupAddress, String dropoffAddress, PaymentMode paymentMode) {
        this.bookingId = bookingId;
        this.riderId = riderId;
        this.riderName = riderName;
        this.riderPhone = riderPhone;
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.baseFare = baseFare;
        this.distanceFare = distanceFare;
        this.taxes = taxes;
        this.commissionFee = commissionFee;
        this.totalFare = totalFare;
        this.pickupAddress = pickupAddress;
        this.dropoffAddress = dropoffAddress;
        if(paymentMode == null) this.paymentMode = PaymentMode.WALLET;
        else this.paymentMode = paymentMode;
    }

    @PrePersist
    protected void onPersist() {
        timestamp = LocalDateTime.now();
    }
}