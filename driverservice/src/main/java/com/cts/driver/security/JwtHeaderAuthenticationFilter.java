package com.cts.driver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cts.driver.config.KafkaConsumerConfig;

import java.io.IOException;
import java.util.List;

/**
 * LAYER 2 SECURITY FILTER
 * Reads the X-User-* headers and creates a SecurityContext.
 */
@Component
// @Slf4j
public class JwtHeaderAuthenticationFilter extends OncePerRequestFilter {
        private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

   
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        

        String userId = request.getHeader("X-User-Id");
        String email = request.getHeader("X-User-Email");
        String rolesHeader = request.getHeader("X-User-Role"); 

        if (email != null && rolesHeader != null && userId != null) {
            String springRole = "ROLE_" + rolesHeader.toUpperCase();
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(springRole));

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    email, null, authorities);
            
            auth.setDetails(userId); // Store the User ID

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.trace("Created SecurityContext for user {} with role {}", email, springRole);
        }

        filterChain.doFilter(request, response);
    }
}