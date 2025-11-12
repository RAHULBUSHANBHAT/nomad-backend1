package com.cts.rider.config;

import feign.Logger; // <-- IMPORT THIS (for the new bean)
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j; // <-- IMPORT THIS (for logging)
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * This is the correct, header-propagating Feign configuration.
 * It intercepts all outgoing Feign calls and copies the security
 * headers from the original incoming request.
 */
@Configuration
@Slf4j // <-- ADD THIS ANNOTATION
public class FeignClientConfig {

     private static final String GATEWAY_KEY_HEADER = "X-Gateway-Key";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
     private static final String USER_EMAIL_HEADER = "X-User-Email";
    // VVVVVV   THIS IS THE NEW BEAN YOU MUST ADD   VVVVVV
    /**
     * BEAN #1: THE FEIGN LOGGER
     * This bean tells Feign to use its "FULL" internal logger.
     * This is the "what" - it tells Feign to log everything.
     * This bean is *required* to see the internal request/response.
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        log.info("Setting Feign Logger Level to FULL.");
        return Logger.Level.FULL;
    }
    // ^^^^^^   THIS IS THE NEW BEAN   ^^^^^^


 @Bean
 public RequestInterceptor requestInterceptor() {
return requestTemplate -> 
{ HttpServletRequest request = getCurrentHttpRequest();

if (request != null) {
                // This log proves your interceptor is running
                log.info("Interceptor is RUNNING. Propagating headers from incoming request...");
                
 // Propagate all 4 headers
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
 return null; // Not in a request context (e.g., background thread)
}
 }

 private void copyHeader(HttpServletRequest request, RequestTemplate template, String headerName) {
 String headerValue = request.getHeader(headerName);
if (headerValue != null) {
            // This log proves it is finding and adding each header
            log.info("Interceptor: Attaching Header: {} = {}", headerName, headerValue);
 template.header(headerName, headerValue);
 } else {
            log.warn("Interceptor: Header '{}' not found in original request.", headerName);
        }
 }
}