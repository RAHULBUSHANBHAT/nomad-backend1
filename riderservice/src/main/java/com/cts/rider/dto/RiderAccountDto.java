package com.cts.rider.dto;

import lombok.Builder;
import lombok.Data;

// This is the combined DTO for the "My Account" page
@Data
@Builder
public class RiderAccountDto {
    // From UserDto
    private String userId;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String city;
    private String state;
    private float userRating;
    
    // From WalletDto
    private double currentBalance;
    private String walletStatus;
}