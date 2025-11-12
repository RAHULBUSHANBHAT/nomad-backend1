package com.cts.booking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * This is the "Fare Board" entity you asked for.
 * Matches your "location_pricing" mock data.
 */
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
    private String vehicleType; // e.g., "SEDAN", "SUV"

    @Column(nullable = false)
    private double baseFare; // e.g., 50.0

    @Column(name = "price_per_km", nullable = false)
    private double ratePerKm; // e.g., 12.0
    
    @Column(nullable = false)
    private double ratePerMinute; // e.g., 1.0
}