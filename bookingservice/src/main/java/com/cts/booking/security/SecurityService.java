package com.cts.booking.security;

import com.cts.booking.model.Booking;
import com.cts.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * LAYER 3 SECURITY HELPER
 * For data-level checks in @PreAuthorize.
 */
@Service("securityService")
public class SecurityService {

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Checks if the authenticated user is the RIDER for this booking.
     */
    public boolean isRiderOnBooking(Authentication authentication, String bookingId) {
        String authenticatedUserId = (String) authentication.getDetails();
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        
        return booking != null && booking.getRiderUserId().equals(authenticatedUserId);
    }

    /**
     * Checks if the authenticated user is the DRIVER for this booking.
     */
    public boolean isDriverOnBooking(Authentication authentication, String bookingId) {
        String authenticatedUserId = (String) authentication.getDetails();
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        
        return booking != null && booking.getDriverUserId() != null && booking.getDriverUserId().equals(authenticatedUserId);
    }
}