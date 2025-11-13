package com.cts.booking.dto.client;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserDto {
    private String id;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private float rating; // Important for the UI
    private String city;
    private LocalDateTime createdAt;
}