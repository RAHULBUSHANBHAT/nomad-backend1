package com.cts.driver.controller;

import com.cts.driver.config.KafkaConsumerConfig;
import com.cts.driver.dto.DriverStatsDto;
import com.cts.driver.dto.UpdateDriverStatusDto;
import com.cts.driver.dto.VehicleTypeCountDto;
import com.cts.driver.service.DriverServiceImpl;
import com.cts.driver.service.VehicleServiceImpl;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/drivers")
public class DriverInternalController {
private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Autowired
    private DriverServiceImpl driverService;
    @Autowired
    private VehicleServiceImpl vehicleService;
    
    @PostMapping("/{userId}/availability")
    public ResponseEntity<Boolean> setAvailability(@PathVariable String userId, @RequestParam boolean available) {
        log.info("Internal request: Setting availability for user {} to {}", userId, available);
        return ResponseEntity.ok(driverService.updateDriverStatus(userId, available));
    }

    @GetMapping("/count")
    public DriverStatsDto getDriversAndVehiclesCount() {
        log.info("Internal request: getDriverCount");
        return driverService.getDriversAndVehiclesCount();
    }

    @GetMapping("/vehicle-types/{city}")
    public ResponseEntity<List<VehicleTypeCountDto>> getAvailableVehiclesInCity(@PathVariable String city) {
        log.info("Internal request: Fetching available vehicles for city {}", city);
        return ResponseEntity.ok(vehicleService.getAvailableVehicleCountsByCity(city));
    }

    @PutMapping("/me/status/{userId}")
    public void updateMyLocation(@PathVariable String userId, @Valid @RequestBody UpdateDriverStatusDto statusDto) {
        driverService.updateDriverStatus(userId, statusDto);
    }
}
