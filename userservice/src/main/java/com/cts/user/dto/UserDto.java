package com.cts.user.dto;

import com.cts.user.model.Role;
import com.cts.user.model.Status;
import lombok.Data;
import java.time.LocalDateTime;

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

