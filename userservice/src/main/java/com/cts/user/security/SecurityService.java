package com.cts.user.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    public boolean isOwner(Authentication authentication, String userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String authenticatedUserId = (String) authentication.getDetails();
        
        return authenticatedUserId != null && authenticatedUserId.equals(userId);
    }
}