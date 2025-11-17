package com.cts.driver.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    private static final String GATEWAY_KEY_HEADER = "X-Gateway-Key";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_EMAIL_HEADER = "X-User-Email";

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            HttpServletRequest request = getCurrentHttpRequest();

            if (request != null) {
                copyHeader(request, requestTemplate, GATEWAY_KEY_HEADER);
                copyHeader(request, requestTemplate, USER_ID_HEADER);
                copyHeader(request, requestTemplate, USER_ROLE_HEADER);
                copyHeader(request, requestTemplate, USER_EMAIL_HEADER);
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
            template.header(headerName, headerValue);
        }
    }
}