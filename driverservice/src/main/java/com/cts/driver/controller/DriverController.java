package com.cts.driver.controller;

import com.cts.driver.dto.DriverProfileDto;
import com.cts.driver.dto.RideOfferDto;
import com.cts.driver.dto.UpdateDriverStatusDto;
import com.cts.driver.dto.UpdateVerificationDto;
import com.cts.driver.model.Driver;
import com.cts.driver.service.DriverServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers") // Aligns with gateway's public route
@Slf4j
public class DriverController {

    @Autowired
    private DriverServiceImpl driverService;
    
    // Helper to get the User ID from the SecurityContext
    private String getUserId(Authentication authentication) {
        return (String) authentication.getDetails();
    }

    // --- DRIVER ENDPOINTS (for the driver's own app) ---

    @GetMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverProfileDto> getMyDriverProfile(Authentication authentication) {
        return ResponseEntity.ok(driverService.getDriverProfileByUserId(getUserId(authentication)));
    }

    @PutMapping("/me/verification")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverProfileDto> updateVerificationDetails(Authentication authentication, 
                                                                    @Valid @RequestBody UpdateVerificationDto verificationDto) {
        return ResponseEntity.ok(driverService.updateVerification(getUserId(authentication), verificationDto));
    }
    
    @PostMapping("/me/status")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Boolean> updateMyLocation(Authentication authentication, 
                                            @Valid @RequestBody UpdateDriverStatusDto statusDto) {
        return ResponseEntity.ok(driverService.updateDriverStatus(getUserId(authentication), statusDto));
    }
    
    @GetMapping("/me/offers")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<RideOfferDto>> getPendingOffers(Authentication authentication) {
        return ResponseEntity.ok(driverService.getPendingOffers(getUserId(authentication)));
    }
    
    @PostMapping("/me/offers/{offerId}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> acceptOffer(Authentication authentication, @PathVariable String offerId) {
        driverService.acceptOffer(getUserId(authentication), offerId);
        return ResponseEntity.ok().build();
    }
    
    // --- ADMIN ENDPOINTS (for the admin panel) ---

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DriverProfileDto>> getAllDrivers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(driverService.getAllDrivers(pageable));
    }

    @GetMapping("/admin/verification-queue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Driver>> getVerificationQueue(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(driverService.getVerificationQueue(pageable));
    }
    
    @PostMapping("/admin/approve/{driverId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveDriver(@PathVariable String driverId) {
        driverService.approveDriver(driverId);
        return ResponseEntity.ok().build();
    }
}