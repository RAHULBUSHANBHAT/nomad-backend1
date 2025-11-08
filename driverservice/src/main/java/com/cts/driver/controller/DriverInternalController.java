package com.cts.driver.controller;

import com.cts.driver.config.KafkaConsumerConfig;
import com.cts.driver.dto.UpdateDriverStatusDto;
import com.cts.driver.model.Driver;
import com.cts.driver.service.DriverServiceImpl;
// import lombok.extern.slf4j.Slf4j;

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

    /**
     * Internal endpoint for booking-service to set a
     * driver's availability (e.g., after a ride is completed).
     * This uses the DRIVER'S *USER* ID.
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
}