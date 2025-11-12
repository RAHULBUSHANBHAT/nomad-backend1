package com.cts.booking.controller;

import com.cts.booking.dto.client.BookingAssignmentDto;
import com.cts.booking.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for SECURE, INTERNAL, service-to-service communication.
 * Secured *only* by Layer 1 (GatewayKeyFilter).
 */
@RestController
@RequestMapping("/api/v1/internal/bookings")
@Slf4j
public class BookingInternalController {

    @Autowired
    private BookingService bookingService;

    /**
     * This is the endpoint our driver-service (Matcher) calls
     * to claim a ride for a driver.
     */
    @PutMapping("/{bookingId}/assign")
    public ResponseEntity<Void> assignDriverToBooking(
            @PathVariable String bookingId,
            @RequestBody BookingAssignmentDto dto) {
        
        log.info("Internal request: Assigning driver {} to booking {}", dto.getDriverUserId(), bookingId);
        bookingService.assignDriverToBooking(bookingId, dto.getDriverUserId());
        return ResponseEntity.ok().build();
    }
}