package com.cts.booking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fare_config")
public class FareConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String vehicleType;

    @Column(nullable = false)
    private double baseFare;

    @Column(name = "price_per_km", nullable = false)
    private double ratePerKm;
}