package com.cts.driver.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class DriverProfileDto {
    private String userId;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String city;
    private String state;
    private float userRating;
    private String userStatus;
    private String driverId;
    private boolean available;
    private String currentCity;
    private String aadharNumber;
    private boolean isAadhaarVerified;
    private String licenseNumber;
    private boolean isDriverLicenseVerified;
    private LocalDate driverLicenseExpiry;
}