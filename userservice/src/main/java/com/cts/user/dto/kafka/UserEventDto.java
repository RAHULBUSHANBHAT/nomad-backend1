package com.cts.user.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDto {
    private String userId;
    private String email;
    private String password; // The HASHED password
    private String role;
    private String eventType; // e.g., "USER_CREATED"
}