package com.cts.service;

import com.cts.dto.LoginRequestDto;
import com.cts.dto.LoginResponseDto;
import com.cts.model.UserCredential;
import com.cts.repository.UserCredentialRepository;
import com.cts.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
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
     */
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        // 1. Find user by email from the *local* database
        UserCredential user = repository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: No user found for email {}", loginRequest.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });

        // 2. Check if the provided password matches the stored hash
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        // 3. Passwords match! Generate a JWT.
        log.info("Login successful for email: {}", loginRequest.getEmail());
        String token = jwtUtil.generateToken(user);

        // 4. Return the response DTO
        return new LoginResponseDto(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );
    }
}