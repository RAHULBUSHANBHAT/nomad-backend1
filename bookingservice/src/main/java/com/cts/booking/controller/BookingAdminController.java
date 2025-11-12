package com.cts.booking.controller;

import com.cts.booking.dto.BookingDto;
import com.cts.booking.dto.FareConfigDto;
import com.cts.booking.model.FareConfig;
import com.cts.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing Fares and viewing all Bookings.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')") // All endpoints are for Admins
@Slf4j
public class BookingAdminController {

    @Autowired
    private BookingService bookingService;

    // --- "FARE BOARD" ENDPOINTS ---

    @GetMapping("/fares")
    public ResponseEntity<List<FareConfig>> getAllFares() {
        log.info("Admin request: getAllFares");
        return ResponseEntity.ok(bookingService.getAllFares());
    }

    @PostMapping("/fares")
    public ResponseEntity<FareConfig> setFare(@Valid @RequestBody FareConfigDto dto) {
        log.info("Admin request: setFare for city {}", dto.getCity());
        return ResponseEntity.ok(bookingService.setFare(dto));
    }
    
    // We would add PUT /fares/{id} and DELETE /fares/{id} here

    // --- BOOKING LEDGER ENDPOINT ---21
    
    @GetMapping("/bookings/all")
    public ResponseEntity<Page<BookingDto>> getAllBookings(
            @PageableDefault(size = 50, sort = "requestTime") Pageable pageable) {
        log.info("Admin request: getAllBookings");
        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }
}