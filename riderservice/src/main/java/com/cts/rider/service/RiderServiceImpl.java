package com.cts.rider.service;

import com.cts.rider.client.UserClient;
import com.cts.rider.dto.RiderAccountDto;
import com.cts.rider.dto.UpdateRiderDto;
import com.cts.rider.dto.client.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // <-- Import
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RiderServiceImpl {

    @Autowired
    private UserClient userClient;

    /**
     * Gets all account info for the "My Account" page.
     */
    public RiderAccountDto getMyAccountDetails(Authentication authentication) {
        log.debug("Fetching account details for current rider...");
        
        // Get the userId from the SecurityContext
        String userId = (String) authentication.getDetails();

        // Make the INTERNAL Feign calls
        UserDto user = userClient.getUserByIdInternal(userId);

        // Combine them into a single response
        return RiderAccountDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .city(user.getCity())
                .state(user.getState())
                .userRating(user.getRating())
                .build();
    }

    /**
     * Updates the user's profile information.
     */
    public UserDto  updateMyAccountDetails(Authentication authentication, UpdateRiderDto updateDto) {
        log.debug("Updating account details for current rider...");
        String userId = (String) authentication.getDetails();
        // Call the new INTERNAL update endpoint
        return userClient.updateUserInternal(userId, updateDto);
    }
}