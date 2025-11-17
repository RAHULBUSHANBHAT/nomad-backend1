package com.cts.service;

import com.cts.dto.LoginRequestDto;
import com.cts.dto.LoginResponseDto;
import com.cts.model.Status;
import com.cts.model.UserCredential;
import com.cts.repository.UserCredentialRepository;
import com.cts.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        UserCredential user = repository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: No user found for email {}", loginRequest.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });

        if (user.getStatus() == Status.SUSPENDED) {
            log.warn("Login failed: Account for email {} is SUSPENDED.", loginRequest.getEmail());
            throw new DisabledException("Your account has been suspended. Please contact support.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        log.info("Login successful for email: {}", loginRequest.getEmail());
        String token = jwtUtil.generateToken(user);

        return new LoginResponseDto(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );
    }
}