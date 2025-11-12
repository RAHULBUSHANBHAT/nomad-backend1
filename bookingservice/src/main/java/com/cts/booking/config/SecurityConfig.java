package com.cts.booking.config;

import com.cts.booking.security.GatewayKeyFilter;
import com.cts.booking.security.JwtHeaderAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Layer 3
public class SecurityConfig {

    @Autowired
    private JwtHeaderAuthenticationFilter jwtHeaderAuthenticationFilter; // Layer 2
    @Autowired
    private GatewayKeyFilter gatewayKeyFilter; // Layer 1

    /**
     * --- "INTERNAL" SECURITY CHAIN ---
     * @Order(1) - Runs FIRST.
     * Applies ONLY to "/api/v1/internal/**"
     */
    @Bean
    @Order(1)
    public SecurityFilterChain internalApiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/internal/**") // Only for internal paths
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Trust is 100% on the Gateway Key
            )
            // Apply ONLY Layer 1 (The Gateway Key)
            .addFilterBefore(gatewayKeyFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * --- "PUBLIC" SECURITY CHAIN ---
     * @Order(2) - Runs SECOND.
     * Applies to all other paths (e.g., "/api/v1/bookings/**")
     */
    @Bean
    @Order(2)
    public SecurityFilterChain publicApiSecurity(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 1. Add filters FIRST
            .addFilterBefore(gatewayKeyFilter, UsernamePasswordAuthenticationFilter.class) // Layer 1
            .addFilterAfter(jwtHeaderAuthenticationFilter, GatewayKeyFilter.class) // Layer 2
            
            // 2. Authorize rules LAST
            .authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated()
            );
            
        return http.build();
    }
}