package com.cts.driver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.cts.driver.config.KafkaConsumerConfig;
import java.io.IOException;

@Component
public class GatewayKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${gateway.secret-key}")
    private String gatewaySecret;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
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