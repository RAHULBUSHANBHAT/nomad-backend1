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

    List<Driver> findByCurrentCityAndAvailableTrue(String currentCity);

    @Query("SELECT d FROM Driver d JOIN d.vehicles v WHERE d.available = true AND d.currentCity = :city AND v.vehicleType = :vehicleType AND v.isVerified = true")
    List<Driver> findAvailableDrivers(String city, VehicleType vehicleType);
    
    @Query("SELECT DISTINCT d FROM Driver d LEFT JOIN d.vehicles v " +
           "WHERE d.isAadhaarVerified = false OR d.isDriverLicenseVerified = false " +
           "OR v.isRcVerified = false OR v.isPucVerified = false OR v.isInsuranceVerified = false")
    Page<Driver> findDriversForVerification(Pageable pageable);
    Page<Driver> findByAadharNumberContains(String aadharNumber, Pageable pageable);
    Page<Driver> findByLicenseNumberContains(String licenseNumber, Pageable pageable);
}