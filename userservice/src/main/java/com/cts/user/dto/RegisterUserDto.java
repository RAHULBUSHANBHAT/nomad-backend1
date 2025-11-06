package com.cts.user.dto;

import com.cts.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserDto {
    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15)
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Role is required (RIDER or DRIVER)")
    private Role role; // RIDER, DRIVER
}