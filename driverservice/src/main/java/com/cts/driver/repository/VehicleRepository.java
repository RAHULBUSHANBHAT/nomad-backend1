package com.cts.driver.repository;

import com.cts.driver.dto.VehicleTypeCountDto;
import com.cts.driver.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
@Query(value = "SELECT * FROM vehicles WHERE driver_id = :driverId", nativeQuery = true)
    List<Vehicle> findByDriverId(@Param("driverId") String driverId);

    @Query("SELECT v.vehicleType as vehicleType, COUNT(v) as availableCount " +
        "FROM Driver d JOIN d.vehicles v " +
        "WHERE d.available = true " +
        "AND d.currentCity = :city " +
        "AND v.isVerified = true " +
        "GROUP BY v.vehicleType")
    List<VehicleTypeCountDto> getAvailableVehicleCountsByCity(@Param("city") String city);
}