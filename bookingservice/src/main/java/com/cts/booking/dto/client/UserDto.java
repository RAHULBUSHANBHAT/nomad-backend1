package com.cts.booking.dto.client;

import lombok.Data;

@Data
public class UserDto {
    private String id;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private float rating; // Important for the UI
}