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
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtHeaderAuthenticationFilter jwtHeaderAuthenticationFilter;
    @Autowired
    private GatewayKeyFilter gatewayKeyFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain internalApiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/internal/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() 
            )
            .addFilterBefore(gatewayKeyFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain publicApiSecurity(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated()
            )
            .addFilterBefore(gatewayKeyFilter, UsernamePasswordAuthenticationFilter.class) // Layer 1
            .addFilterAfter(jwtHeaderAuthenticationFilter, GatewayKeyFilter.class); // Layer 2
            
        return http.build();
    }
}