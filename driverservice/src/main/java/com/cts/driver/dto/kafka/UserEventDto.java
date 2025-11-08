package com.cts.driver.dto.kafka;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// This DTO must match the one from user-service
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDto {
    private String userId;
    private String email;
    private String password;
    private String role;
    private String eventType;
}