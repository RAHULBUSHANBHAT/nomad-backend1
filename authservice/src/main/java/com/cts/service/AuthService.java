package com.cts.service;

import com.cts.dto.LoginRequestDto;
import com.cts.dto.LoginResponseDto;
import com.cts.model.Status; // 1. Import the Status enum
import com.cts.model.UserCredential;
import com.cts.repository.UserCredentialRepository;
import com.cts.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException; // 2. Use a more specific exception
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserCredentialRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Authenticates a user and generates a JWT.
     *
     * @param loginRequest DTO containing email and password
     * @return LoginResponseDto containing the JWT and user info
     * @throws BadCredentialsException if credentials are invalid
     * @throws DisabledException if the user account is suspended
     */
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        // 1. Find user by email
        UserCredential user = repository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: No user found for email {}", loginRequest.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });

        // 2. NEW: Check if the user is suspended
        if (user.getStatus() == Status.SUSPENDED) {
            log.warn("Login failed: Account for email {} is SUSPENDED.", loginRequest.getEmail());
            // Throwing DisabledException is more semantically correct
            throw new DisabledException("Your account has been suspended. Please contact support.");
        }

        // 3. Check password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        // 4. Passwords match! Generate a JWT.
        log.info("Login successful for email: {}", loginRequest.getEmail());
        String token = jwtUtil.generateToken(user);

        // 5. Return the response DTO
        return new LoginResponseDto(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );
    }
}