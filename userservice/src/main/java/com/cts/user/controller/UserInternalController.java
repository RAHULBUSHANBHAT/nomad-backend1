package com.cts.user.controller;

import com.cts.user.dto.UserDto;
import com.cts.user.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        // We don't need @PreAuthorize here because only trusted services
        // (who have the Gateway Key) can even call this.
        return ResponseEntity.ok(userService.getUserById(id));
    }
}