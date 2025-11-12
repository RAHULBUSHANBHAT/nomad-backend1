package com.cts.user.dto;

import com.cts.user.model.Role;
import com.cts.user.model.Status;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * A "safe" DTO to return from API endpoints.
 * Notice it does NOT contain the password.
 */
@Data
public class UserDto {
    private String id;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String city;
    private String state;
    private boolean isEmailVerified;
    private Role role;
    private Status status;
    private float rating;
    private int totalRatings;
    private LocalDateTime createdAt;
}

