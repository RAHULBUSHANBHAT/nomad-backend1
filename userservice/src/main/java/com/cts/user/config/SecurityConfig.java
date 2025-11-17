package com.cts.user.config;

import com.cts.user.security.GatewayKeyFilter;
import com.cts.user.security.JwtHeaderAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
     * This is now our ONE and ONLY security chain for the entire service.
     * It correctly applies all 3 layers of security.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/users/register").permitAll()
                .anyRequest().authenticated()
            )
            // 3. We apply our filters IN ORDER.
            
            // Layer 1: Check the Gateway Key *first* for ALL requests.
            .addFilterBefore(gatewayKeyFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Layer 2: If Layer 1 passes, read the User Headers and create
            //          the SecurityContext for *all* requests.
            .addFilterAfter(jwtHeaderAuthenticationFilter, GatewayKeyFilter.class);
            
            // Layer 3: @PreAuthorize will now work on *all* controllers,
            //          including the internal ones.

        return http.build();
    }
}