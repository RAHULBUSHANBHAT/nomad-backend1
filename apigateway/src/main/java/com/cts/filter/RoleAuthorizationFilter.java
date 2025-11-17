package com.cts.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;
import lombok.Data;

@Component
public class RoleAuthorizationFilter extends AbstractGatewayFilterFactory<RoleAuthorizationFilter.Config> {
    private static final Logger log = LoggerFactory.getLogger(RoleAuthorizationFilter.class);

    public RoleAuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String userRole = exchange.getRequest().getHeaders().getFirst("X-User-Role");

            if (userRole == null) {
                log.warn("Role Filter FAILED - 'X-User-Role' header is missing.");
                return onError(exchange, "Role not found in token", HttpStatus.FORBIDDEN);
            }

            List<String> allowedRoles = config.getAllowedRoles();
            boolean hasPermission = allowedRoles.stream()
                    .anyMatch(role -> role.equalsIgnoreCase(userRole));

            if (!hasPermission) {
                log.warn("Role Filter FAILED - Access Denied. User role: [{}], Required roles: {}",
                         userRole, allowedRoles);
                return onError(exchange,
                        "Access denied. Required roles: " + allowedRoles +
                                ", but you have: " + userRole,
                        HttpStatus.FORBIDDEN);
            }

            log.info("Role Filter SUCCESS - User role: [{}], Path: {}",
                     userRole, exchange.getRequest().getPath());
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("X-Error-Message", error);
        return exchange.getResponse().setComplete();
    }    
    
    @Data    
    public static class Config {
        private List<String> allowedRoles;
        public List<String> getAllowedRoles() { return allowedRoles; }
        public void setAllowedRoles(List<String> allowedRoles) { this.allowedRoles = allowedRoles; }
        public void setRequiredRole(String role) { this.allowedRoles = Arrays.asList(role); }
    }

}