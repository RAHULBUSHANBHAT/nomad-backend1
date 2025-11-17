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

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class BookingAdminController {

    @Autowired
    private BookingService bookingService;

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

    @PutMapping("/fares/{id}")
    public ResponseEntity<FareConfig> updateFare(@PathVariable String id, @Valid @RequestBody FareConfigDto dto) {
        log.info("Admin request: deleting fare with id {}", id);
        return ResponseEntity.ok(bookingService.updateFare(id, dto));
    }

    @DeleteMapping("/fares/{id}")
    public ResponseEntity<FareConfig> deleteFare(@PathVariable String id) {
        log.info("Admin request: deleting fare with id {}", id);
        bookingService.deleteFare(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/bookings/all")
    public ResponseEntity<Page<BookingDto>> getAllBookings(
            @PageableDefault(size = 50, sort = "requestTime") Pageable pageable) {
        log.info("Admin request: getAllBookings");
        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }
}