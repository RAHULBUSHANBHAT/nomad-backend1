package com.cts.rider.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiderAccountDto {
    private String userId;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String city;
    private String state;
    private float userRating;
}