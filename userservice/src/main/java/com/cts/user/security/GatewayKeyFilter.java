package com.cts.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class GatewayKeyFilter extends OncePerRequestFilter {

    @Value("${gateway.secret-key}")
    private String gatewaySecret;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        if (request.getRequestURI().equals("/api/v1/users/register")) {
            log.trace("Bypassing GatewayKeyFilter for public registration endpoint.");
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getHeader("X-Gateway-Key");

        if (key == null || !key.equals(gatewaySecret)) {
            log.warn("Blocked request without valid Gateway Key from IP: {}", request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Invalid or missing gateway token.");
            return;
        }

        log.trace("Gateway Key validated. Proceeding.");
        filterChain.doFilter(request, response);
    }
}