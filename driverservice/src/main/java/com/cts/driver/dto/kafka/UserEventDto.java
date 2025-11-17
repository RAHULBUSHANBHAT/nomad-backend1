package com.cts.driver.dto.kafka;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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