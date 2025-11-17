package com.cts.driver.controller;

import com.cts.driver.config.KafkaConsumerConfig;
import com.cts.driver.dto.VehicleDto;
import com.cts.driver.service.VehicleServiceImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1") // Nested under the driver
@PreAuthorize("hasRole('DRIVER')") // All methods require DRIVER role
public class VehicleController {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Autowired
    private VehicleServiceImpl vehicleService;

    private String getUserId(Authentication authentication) {
        return (String) authentication.getDetails();
    }

    @PostMapping("/drivers/me/vehicles")
    public ResponseEntity<VehicleDto> addVehicle(Authentication authentication, 
                                                 @Valid @RequestBody VehicleDto vehicleDto) {
        log.info("Driver {} adding vehicle", getUserId(authentication));
        VehicleDto newVehicle = vehicleService.addVehicle(getUserId(authentication), vehicleDto);
        return new ResponseEntity<>(newVehicle, HttpStatus.CREATED);
    }

    @GetMapping("/drivers/me/vehicles")
    public ResponseEntity<List<VehicleDto>> getMyVehicles(Authentication authentication) {
        log.info("Driver {} fetching vehicles", getUserId(authentication));
        return ResponseEntity.ok(vehicleService.getVehiclesByDriverUserId(getUserId(authentication)));
    }
    
    @DeleteMapping("/drivers/me/vehicles/{vehicleId}")
    @PreAuthorize("@securityService.isVehicleOwner(authentication, #vehicleId)") // Layer 3 data check
    public ResponseEntity<?> deleteVehicle(Authentication authentication, 
                                             @PathVariable String vehicleId) {
        log.info("Driver {} deleting vehicle {}", getUserId(authentication), vehicleId);
        vehicleService.deleteVehicle(getUserId(authentication), vehicleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/drivers/me/vehicles/{vehicleId}")
    @PreAuthorize("@securityService.isVehicleOwner(authentication, #vehicleId)") // Layer 3
    public ResponseEntity<VehicleDto> updateVehicle(Authentication authentication, 
                                                    @PathVariable String vehicleId,
                                                    @Valid @RequestBody VehicleDto vehicleDto) {
        log.info("Driver {} updating vehicle {}", getUserId(authentication), vehicleId);
        VehicleDto updatedVehicle = vehicleService.updateVehicle(getUserId(authentication), vehicleId, vehicleDto);
        return ResponseEntity.ok(updatedVehicle);
    }
}