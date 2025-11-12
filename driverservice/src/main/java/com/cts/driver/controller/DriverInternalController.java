package com.cts.driver.controller;

import com.cts.driver.config.KafkaConsumerConfig;
import com.cts.driver.dto.UpdateDriverStatusDto;
import com.cts.driver.dto.VehicleTypeCountDto;
import com.cts.driver.model.Driver;
import com.cts.driver.service.DriverServiceImpl;
import com.cts.driver.service.VehicleServiceImpl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
// import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/internal/drivers")

public class DriverInternalController {
private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Autowired
    private DriverServiceImpl driverService;
    @Autowired
    private VehicleServiceImpl vehicleService;

    /**
     * Internal endpoint for booking-service to set a
     * driver's availability (e.g., after a ride is completed).
     * This uses the DRIVER'S *USER* ID.
     * 
     * 
     */

    
    @PostMapping("/{userId}/availability")
    public ResponseEntity<Boolean> setAvailability(@PathVariable String userId, @RequestParam boolean available) {
        log.info("Internal request: Setting availability for user {} to {}", userId, available);
        
        Driver driver = driverService.findDriverByUserId(userId);
        UpdateDriverStatusDto dto = new UpdateDriverStatusDto();
        dto.setAvailable(available);
        dto.setCurrentCity(driver.getCurrentCity()); // Keep last known city
        
        return ResponseEntity.ok(driverService.updateDriverStatus(userId, dto));
    }

    @GetMapping("/count")
    public long getDriverCount() {
        log.info("Internal request: getDriverCount");
        return driverService.getDriverCount();
    }

    @GetMapping("/vehicle-types/{city}")
    public ResponseEntity<List<VehicleTypeCountDto>> getAvailableVehiclesInCity(@PathVariable String city) {
        log.info("Internal request: Fetching available vehicles for city {}", city);
        return ResponseEntity.ok(vehicleService.getAvailableVehicleCountsByCity(city));
    }
}
