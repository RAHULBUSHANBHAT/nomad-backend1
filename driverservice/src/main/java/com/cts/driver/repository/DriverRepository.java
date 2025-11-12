package com.cts.driver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cts.driver.model.Driver;
import com.cts.driver.model.VehicleType;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    
    Optional<Driver> findByUserId(String userId);

    // --- THIS WAS MISSING ---
    List<Driver> findByCurrentCityAndAvailableTrue(String currentCity);

    // Custom query if you want to filter by specific vehicle type in DB (Optional usage)
    @Query("SELECT d FROM Driver d JOIN d.vehicles v WHERE d.available = true AND d.currentCity = :city AND v.vehicleType = :vehicleType AND v.isVerified = true")
    List<Driver> findAvailableDrivers(String city, VehicleType vehicleType);
    
    // For admin "verification queue"
    Page<Driver> findByIsDriverLicenseVerifiedFalse(Pageable pageable);
    Optional<Driver> findByAadharNumber(String aadharNumber);
    Optional<Driver> findByLicenseNumber(String licenseNumber);
}