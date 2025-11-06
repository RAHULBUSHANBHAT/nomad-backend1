package com.cts.user.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * LAYER 3 SECURITY HELPER
 * This bean can be called from within @PreAuthorize annotations
 * to perform complex, data-level security checks.
 */
@Service("securityService") // Bean name to reference in SpEL
public class SecurityService {

    /**
     * Checks if the currently authenticated user (from the token)
     * is the same as the user ID being requested.
     * Usage: @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #id)")
     */
    public boolean isOwner(Authentication authentication, String userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // We stored the userId in the 'details' of the authentication object
        String authenticatedUserId = (String) authentication.getDetails();
        
        return authenticatedUserId != null && authenticatedUserId.equals(userId);
    }
}