package com.cts.driver.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId; // This is the foreign key to the user-service's User

    // --- Verification Details ---
    @Column(name = "aadhar_number", unique = true)
    private String aadharNumber;

    @Column(name = "is_aadhaar_verified")
    private boolean isAadhaarVerified = false;

    @Column(name = "pan_number", unique = true)
    private String panNumber;

    @Column(name = "is_pan_verified")
    private boolean isPanVerified = false;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "is_driver_license_verified")
    private boolean isDriverLicenseVerified = false;

    @Column(name = "driver_license_expiry")
    private LocalDate driverLicenseExpiry;

    // --- Profile & Status ---
    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "available")
    private boolean available = false; // Default to false until verified and online

    @Column(name = "current_city")
    private String currentCity; // For our simple matching

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Vehicle Relationship ---
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "driver_id") 
    private List<Vehicle> vehicles = new ArrayList<>();
    
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