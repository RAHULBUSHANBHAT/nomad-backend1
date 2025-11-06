package com.cts.user.config;

import com.cts.user.security.GatewayKeyFilter;
import com.cts.user.security.JwtHeaderAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // This enables @PreAuthorize (Layer 3)
public class SecurityConfig {

    @Autowired
    private JwtHeaderAuthenticationFilter jwtHeaderAuthenticationFilter; // Layer 2

    @Autowired
    private GatewayKeyFilter gatewayKeyFilter; // Layer 1

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * --- "INTERNAL" SECURITY CHAIN ---
     * @Order(1) - Runs FIRST.
     * Applies ONLY to "/api/v1/internal/**"
     */
    @Bean
    @Order(1)
    public SecurityFilterChain internalApiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/internal/**") // Only applies to these paths
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated() // All internal endpoints must be authenticated
            )
            // Apply ONLY Layer 1 (The Gateway Key)
            .addFilterBefore(gatewayKeyFilter, UsernamePasswordAuthenticationFilter.class);
            // We do NOT add Layer 2 (JwtHeaderAuthenticationFilter)
        
        return http.build();
    }

    /**
     * --- "PUBLIC" SECURITY CHAIN ---
     * @Order(2) - Runs SECOND (for all other paths).
     * Applies to "/api/v1/users/**"
     */
    @Bean
    @Order(2)
    public SecurityFilterChain publicApiSecurity(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // The registration endpoint is public (as defined in gateway)
                .requestMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()
                // All other "public" endpoints must be authenticated
                .anyRequest().authenticated()
            )
            // Apply BOTH Layer 1 AND Layer 2
            .addFilterBefore(gatewayKeyFilter, UsernamePasswordAuthenticationFilter.class) // Layer 1
            .addFilterAfter(jwtHeaderAuthenticationFilter, GatewayKeyFilter.class); // Layer 2

        return http.build();
    }
}