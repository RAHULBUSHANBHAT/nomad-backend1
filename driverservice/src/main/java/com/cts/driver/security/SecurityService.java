package com.cts.driver.security;

import com.cts.driver.model.Driver;
import com.cts.driver.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service("securityService")
public class SecurityService {
    

    @Autowired
    private DriverRepository driverRepository;

    public boolean isVehicleOwner(Authentication authentication, String vehicleId) {
        String authenticatedUserId = (String) authentication.getDetails();
        if (authenticatedUserId == null) return false;

        Optional<Driver> driverOpt = driverRepository.findByUserId(authenticatedUserId);
        if (driverOpt.isEmpty()) return false;

        return driverOpt.get().getVehicles().stream()
                .anyMatch(vehicle -> vehicle.getId().equals(vehicleId));
    }
}