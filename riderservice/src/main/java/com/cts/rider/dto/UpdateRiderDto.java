package com.cts.rider.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

// DTO to send to user-service for updates
@Data
public class UpdateRiderDto {
    @Size(min = 1, max = 50)
    private String firstName;
    @Size(min = 1, max = 50)
    private String lastName;
    @Size(min = 1, max = 50)
    private String city;
    @Size(min = 1, max = 50)
    private String state;
}