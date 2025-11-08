package com.cts.driver.mapper;

import com.cts.driver.dto.DriverProfileDto;
import com.cts.driver.dto.client.UserDto;
import com.cts.driver.model.Driver;
import org.springframework.stereotype.Component;

@Component
public class DriverMapper {
    
    public DriverProfileDto toDriverProfileDto(Driver driver, UserDto user) {
        return DriverProfileDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .city(user.getCity())
                .state(user.getState())
                .userRating(user.getRating())
                .userStatus(user.getStatus())
                .driverId(driver.getId())
                .available(driver.isAvailable())
                .currentCity(driver.getCurrentCity())
                .aadharNumber(driver.getAadharNumber())
                .isAadhaarVerified(driver.isAadhaarVerified())
                .panNumber(driver.getPanNumber())
                .isPanVerified(driver.isPanVerified())
                .licenseNumber(driver.getLicenseNumber())
                .isDriverLicenseVerified(driver.isDriverLicenseVerified())
                .driverLicenseExpiry(driver.getDriverLicenseExpiry())
                .build();
    }
}