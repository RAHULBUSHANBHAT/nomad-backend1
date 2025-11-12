package com.cts.driver.repository;

import com.cts.driver.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository


public interface VehicleRepository extends JpaRepository<Vehicle, String> {
@Query(value = "SELECT * FROM vehicles WHERE driver_id = :driverId", nativeQuery = true)
    List<Vehicle> findByDriverId(@Param("driverId") String driverId);
}