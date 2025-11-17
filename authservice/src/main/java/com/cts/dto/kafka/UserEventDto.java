package com.cts.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDto {
    private String userId;
    private String email;
    private String password;
    private String role;
    private String eventType;
    private String status;
}