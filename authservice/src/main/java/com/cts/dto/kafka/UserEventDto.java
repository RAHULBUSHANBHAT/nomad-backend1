package com.cts.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for consuming user creation events from Kafka.
 * This structure MUST match the UserEventDto from the user-service.
 */
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