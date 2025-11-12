package com.cts.wallet.config;

import com.cts.wallet.security.GatewayKeyFilter;
import com.cts.wallet.security.JwtHeaderAuthenticationFilter;
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
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            .authorizeHttpRequests(authz -> authz
                // Trust the GatewayKeyFilter *is* the authentication.
                .anyRequest().permitAll() 
            )
            // Apply ONLY Layer 1 (The Gateway Key)
            .addFilterBefore(gatewayKeyFilter, UsernamePasswordAuthenticationFilter.class);
            // We do NOT add Layer 2, because this call has no user context.
        
        return http.build();
    }

    /**
     * --- "PUBLIC" SECURITY CHAIN ---
     * @Order(2) - Runs SECOND.
     * Applies to all other paths (e.g., "/api/v1/wallets/**")
     */
    @Bean
    @Order(2)
    public SecurityFilterChain publicApiSecurity(HttpSecurity http) throws Exception {
        http
            // This chain applies to all other requests
            .csrf(csrf -> csrf.disable())
            
            // This is the CORRECTED line
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(authz -> authz
                // All other paths must be authenticated by a user
                .anyRequest().authenticated()
            )
            // Apply BOTH Layer 1 AND Layer 2
            .addFilterBefore(gatewayKeyFilter, UsernamePasswordAuthenticationFilter.class) // Layer 1
            .addFilterAfter(jwtHeaderAuthenticationFilter, GatewayKeyFilter.class); // Layer 2
            
        return http.build();
    }
}