package com.cts.controller;

import com.cts.dto.LoginRequestDto;
import com.cts.dto.LoginResponseDto;
import com.cts.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth") // Aligns with the gateway route
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Handles user login requests.
     * This endpoint is public (as defined in SecurityConfig).
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
// log.info("Received login request for email: {}", loginRequest.getEmail());
        LoginResponseDto response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}        