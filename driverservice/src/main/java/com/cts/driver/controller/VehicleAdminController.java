package com.cts.driver.controller;

import com.cts.driver.config.KafkaConsumerConfig;
import com.cts.driver.dto.VehicleDto;
import com.cts.driver.service.VehicleServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/drivers")
@PreAuthorize("hasRole('ADMIN')")
public class VehicleAdminController {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Autowired
    private VehicleServiceImpl vehicleService;

    @GetMapping("/{driverUserId}/vehicles")
    public ResponseEntity<List<VehicleDto>> getVehiclesByDriverId(@PathVariable String driverUserId) {
        log.info("Admin fetching vehicles for driver {}", driverUserId);
        return ResponseEntity.ok(vehicleService.getVehiclesByDriverUserId(driverUserId));
    }
}