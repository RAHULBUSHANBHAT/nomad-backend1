package com.cts.driver.config;

import com.cts.driver.security.GatewayKeyFilter;
import com.cts.driver.security.JwtHeaderAuthenticationFilter;
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
                .requestMatchers("/api/v1/internal/**").permitAll()
            )
            // Apply ONLY Layer 1 (The Gateway Key)
            .addFilterBefore(gatewayKeyFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * --- "PUBLIC" SECURITY CHAIN ---
     * @Order(2) - Runs SECOND.
     * Applies to all other paths (e.g., "/api/v1/drivers/**")
     */
    @Bean
    @Order(2)
    public SecurityFilterChain publicApiSecurity(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated()
            )
            // Apply BOTH Layer 1 AND Layer 2
            .addFilterBefore(gatewayKeyFilter, UsernamePasswordAuthenticationFilter.class) // Layer 1
            .addFilterAfter(jwtHeaderAuthenticationFilter, GatewayKeyFilter.class); // Layer 2
            
        return http.build();
    }
}