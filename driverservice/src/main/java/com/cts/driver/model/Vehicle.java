package com.cts.driver.model;

import lombok.Data;
import java.time.LocalDate;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;

    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;
    
    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "color", nullable = false)
    private String color;
    
    @Column(name = "is_verified")
    private boolean isVerified = false;

    @Column(name = "rc_number")
    private String rcNumber;

    @Column(name = "is_rc_verified")
    private boolean isRcVerified = false;

    @Column(name = "puc_number")
    private String pucNumber;

    @Column(name = "puc_expiry")
    private LocalDate pucExpiry;

    @Column(name = "is_puc_verified")
    private boolean isPucVerified = false;

    @Column(name = "insurance_policy_number")
    private String insurancePolicyNumber;

    @Column(name = "is_insurance_verified")
    private boolean isInsuranceVerified = false;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}