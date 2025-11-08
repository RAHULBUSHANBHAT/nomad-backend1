package com.cts.driver.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

// DTO to capture response from user-service
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Be resilient to fields we don't care about
public class UserDto {
    private String id;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String city;
    private String state;
    private String role;
    private String status;
    private float rating;
}