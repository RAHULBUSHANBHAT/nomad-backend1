package com.cts.driver.repository;

import com.cts.driver.model.Driver;
import com.cts.driver.model.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    
    Optional<Driver> findByUserId(String userId);

    // This is the core of our "simple matcher"
    // It joins with the vehicles table to check the type
    // @Query("SELECT d FROM Driver d JOIN d.vehicles v WHERE d.available = true AND d.currentCity = :city AND v.vehicleType = :vehicleType AND v.isVerified = true")
    // List<Driver> findAvailableDrivers(String city, VehicleType vehicleType);
    
    // For admin "verification queue"
    Page<Driver> findByIsDriverLicenseVerifiedFalse(Pageable pageable);
}