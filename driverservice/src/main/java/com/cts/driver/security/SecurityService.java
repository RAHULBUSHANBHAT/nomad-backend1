package com.cts.driver.security;

import com.cts.driver.model.Driver;
import com.cts.driver.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * LAYER 3 SECURITY HELPER
 * For use in @PreAuthorize annotations.
 */
@Service("securityService")
public class SecurityService {
    

    @Autowired
    private DriverRepository driverRepository;

    /**
     * Checks if the authenticated driver is the owner of a specific vehicle.
     * Usage: @PreAuthorize("@securityService.isVehicleOwner(authentication, #vehicleId)")
     */
    public boolean isVehicleOwner(Authentication authentication, String vehicleId) {
        String authenticatedUserId = (String) authentication.getDetails();
        if (authenticatedUserId == null) return false;

        Optional<Driver> driverOpt = driverRepository.findByUserId(authenticatedUserId);
        if (driverOpt.isEmpty()) return false;

        // Check if the vehicle is in the driver's list
        return driverOpt.get().getVehicles().stream()
                .anyMatch(vehicle -> vehicle.getId().equals(vehicleId));
    }
}