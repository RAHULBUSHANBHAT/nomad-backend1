package com.cts.driver.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * This is the crucial configuration for service-to-service calls.
 * It intercepts ALL outgoing Feign requests (like to User-Service)
 * and copies the security headers from the INCOMING request (from the Gateway).
 *
 * This "propagates" the user's identity and the gateway's trust.
 */
@Configuration
public class FeignClientConfig {

    // Define the exact header names we need to forward
    private static final String GATEWAY_KEY_HEADER = "X-Gateway-Key";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_EMAIL_HEADER = "X-User-Email";

    /**
     * Creates the RequestInterceptor bean that Feign will automatically use.
     * This bean is a "hook" that runs *before* any Feign request is sent.
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Get the current, incoming HTTP request.
            // This is the request from the Gateway, which contains our "X-" headers.
            HttpServletRequest request = getCurrentHttpRequest();

            if (request != null) {
                // Copy all our security headers from the incoming request
                // to the new, outgoing Feign request.
                copyHeader(request, requestTemplate, GATEWAY_KEY_HEADER);
                copyHeader(request, requestTemplate, USER_ID_HEADER);
                copyHeader(request, requestTemplate, USER_ROLE_HEADER);
                copyHeader(request, requestTemplate, USER_EMAIL_HEADER);
            }
        };
    }

    /**
     * A helper method to safely get the current HttpServletRequest
     * from Spring's RequestContextHolder.
     * This is how we access the "context" of the original call.
     */
    private HttpServletRequest getCurrentHttpRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        } catch (Exception e) {
            // Can happen in background threads. Return null.
            return null;
        }
    }

    /**
     * A helper method to safely copy a header if it exists.
     */
    private void copyHeader(HttpServletRequest request, RequestTemplate template, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (headerValue != null) {
            // This is the line that adds the header to the *outgoing* Feign call
            template.header(headerName, headerValue);
        }
    }
}