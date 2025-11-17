package com.cts.booking.controller;

import com.cts.booking.dto.client.BookingAssignmentDto;
import com.cts.booking.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/bookings")
@Slf4j
public class BookingInternalController {

    @Autowired
    private BookingService bookingService;

    @PutMapping("/{bookingId}/assign")
    public ResponseEntity<Void> assignDriverToBooking(
            @PathVariable String bookingId,
            @RequestBody BookingAssignmentDto dto) {
        
        log.info("Internal request: Assigning driver {} with vehicle {} to booking {}", 
                 dto.getDriverUserId(), dto.getVehicleId(), bookingId);
        
        bookingService.assignDriverToBooking(bookingId, dto.getDriverUserId(), dto.getVehicleId());
        
        return ResponseEntity.ok().build();
    }
}