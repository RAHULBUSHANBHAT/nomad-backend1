package com.cts.booking.repository;

import com.cts.booking.model.FareConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareConfigRepository extends JpaRepository<FareConfig, String> {
    
    // This is the core of the fare calculation
    Optional<FareConfig> findByCityAndVehicleType(String city, String vehicleType);
}