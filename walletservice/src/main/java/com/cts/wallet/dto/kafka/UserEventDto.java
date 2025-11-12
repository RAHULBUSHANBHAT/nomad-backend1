package com.cts.wallet.dto.kafka;

// Import Lombok annotations
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class UserEventDto {

    private String eventType; // e.g., "USER_CREATED"
    private String userId;
    private String email;
    private String password; // This should be the HASHED password
    private String role;     // e.g., "RIDER", "DRIVER"

    // --- Manually Generated Getters and Setters ---

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}