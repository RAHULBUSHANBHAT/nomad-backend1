package com.cts.booking.security;

import com.cts.booking.model.Booking;
import com.cts.booking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    @Autowired
    private BookingRepository bookingRepository;

    public boolean isRiderOnBooking(Authentication authentication, String bookingId) {
        String authenticatedUserId = (String) authentication.getDetails();
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        
        return booking != null && booking.getRiderUserId().equals(authenticatedUserId);
    }

    public boolean isDriverOnBooking(Authentication authentication, String bookingId) {
        String authenticatedUserId = (String) authentication.getDetails();
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        
        return booking != null && booking.getDriverUserId() != null && booking.getDriverUserId().equals(authenticatedUserId);
    }
}