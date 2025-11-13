package com.cts.user.controller;

import com.cts.user.dto.AddRatingRequestDto;
import com.cts.user.dto.UpdateUserDto; // <-- Import
import com.cts.user.dto.UserDto;
import com.cts.user.service.UserServiceImpl;
import jakarta.validation.Valid; // <-- Import
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // <-- Import

/**
 * Internal-facing controller for service-to-service communication.
 * Secured *only* by Layer 1 (GatewayKeyFilter).
 */
@RestController
@RequestMapping("/api/v1/internal/users")
@Slf4j
public class UserInternalController {
    
    @Autowired
    private UserServiceImpl userService;

    /**
     * An internal endpoint for other services (like driver-service)
     * to fetch user data by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserByIdInternal(@PathVariable String id) {
        log.info("Internal request: Fetching user by ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * --- THIS IS THE NEW, MISSING METHOD ---
     * An internal endpoint for other services (like rider-service)
     * to update user data by ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUserInternal(
            @PathVariable String id, 
            @Valid @RequestBody UpdateUserDto updateUserDto) {
        
        log.info("Internal request: Updating user by ID: {}", id);
        UserDto updatedUser = userService.updateUser(id, updateUserDto, "");
        return ResponseEntity.ok(updatedUser);
    }
    
    @PostMapping("/{id}/add-rating")
    public ResponseEntity<Void> addRating(
            @PathVariable String id, 
            @Valid @RequestBody AddRatingRequestDto dto) {
        
        log.info("Internal request: Adding rating of {} to user ID: {}", dto.getRating(), id);
        userService.addRating(id, dto.getRating());
        return ResponseEntity.ok().build();
    }

    
}