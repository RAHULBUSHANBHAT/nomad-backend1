package com.cts.rider.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignClientConfig {

    private static final String GATEWAY_KEY_HEADER = "X-Gateway-Key";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_EMAIL_HEADER = "X-User-Email";

    @Bean
    Logger.Level feignLoggerLevel() {
        log.info("Setting Feign Logger Level to FULL.");
        return Logger.Level.FULL;
    }


    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> 
        {
            HttpServletRequest request = getCurrentHttpRequest();

            if (request != null) {
                log.info("Interceptor is RUNNING. Propagating headers from incoming request...");
                            
            copyHeader(request, requestTemplate, GATEWAY_KEY_HEADER);
            copyHeader(request, requestTemplate, USER_ID_HEADER);
            copyHeader(request, requestTemplate, USER_ROLE_HEADER);
            copyHeader(request, requestTemplate, USER_EMAIL_HEADER);
            } else {
                log.warn("Interceptor: Not in a web request context. Skipping header propagation.");
            }
        };
    }

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private void copyHeader(HttpServletRequest request, RequestTemplate template, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (headerValue != null) {
            log.info("Interceptor: Attaching Header: {} = {}", headerName, headerValue);
        template.header(headerName, headerValue);
        } else {
            log.warn("Interceptor: Header '{}' not found in original request.", headerName);
        }
    }
}