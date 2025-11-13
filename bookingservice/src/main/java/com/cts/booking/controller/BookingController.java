package com.cts.booking.controller;

import com.cts.booking.dto.BookingDto;
import com.cts.booking.dto.BookingFiltersDto;
import com.cts.booking.dto.CreateBookingRequestDto;
import com.cts.booking.dto.AddRatingRequestDto; // <-- ADD
import com.cts.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.GrantedAuthority;

@RestController
@RequestMapping("/api/v1") // Mapped at root to handle all paths
@Slf4j
public class BookingController {

    @Autowired
    private BookingService bookingService;

    private String getUserId(Authentication authentication) {
        return (String) authentication.getDetails();
    }
    
    // Gets the role *without* the "ROLE_" prefix
    private String getUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .orElse("");
    }

    // --- RIDER ENDPOINTS ---
    
    @PostMapping("/bookings/request")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<BookingDto> requestRide(Authentication authentication, 
                                                  @Valid @RequestBody CreateBookingRequestDto dto) {
        log.info("Rider {} is requesting a ride", getUserId(authentication));
        BookingDto newBooking = bookingService.requestRide(getUserId(authentication), dto);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    @GetMapping("/bookings/active-ride/rider")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<BookingDto> getActiveBookingForRider(Authentication authentication) {
        log.info("Rider {} is requesting a their active ride", getUserId(authentication));
        BookingDto newBooking = bookingService.getActiveRideForRider(getUserId(authentication));
        return new ResponseEntity<>(newBooking, HttpStatus.OK);
    }

    @GetMapping("/bookings/active-ride/driver")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<BookingDto> getActiveBookingForDriver(Authentication authentication) {
        log.info("Driver {} is requesting a their active ride", getUserId(authentication));
        BookingDto newBooking = bookingService.getActiveRideForDriver(getUserId(authentication));
        return new ResponseEntity<>(newBooking, HttpStatus.OK);
    }
    
    @PostMapping("/driver/bookings/{id}/confirm-cash")
    @PreAuthorize("hasRole('DRIVER') and @securityService.isDriverOnBooking(authentication, #id)")
    public ResponseEntity<BookingDto> confirmCashPayment(Authentication authentication, @PathVariable("id") String id) {
        return ResponseEntity.ok(bookingService.confirmCashPayment(id, getUserId(authentication)));
    }

    @GetMapping("/bookings/vehicle-types")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<?> getAvailableVehiclesCount(Authentication authentication) {
        log.info("Fetching all available vehicle types");
        return new ResponseEntity<>(bookingService.getVehicleAvailability(getUserId(authentication)), HttpStatus.OK);
    }

    @GetMapping("/bookings/me/history")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<Page<BookingDto>> getRiderBookingHistory(
            Authentication authentication,
            @Valid @RequestParam(required = false) BookingFiltersDto bookingFiltersDto,
            @PageableDefault(size = 20, sort = "requestTime", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Rider {} fetching booking history", getUserId(authentication));
        return ResponseEntity.ok(bookingService.getBookingsForRider(getUserId(authentication), bookingFiltersDto, pageable));
    }

    /**
     * --- NEW "FEEDBACK" ENDPOINT ---
     * Can only be left by the RIDER after a ride.
     */
    @PostMapping("/bookings/{id}/feedback")
    @PreAuthorize("hasRole('RIDER') and @securityService.isRiderOnBooking(authentication, #id)")
    public ResponseEntity<Void> addFeedback(Authentication authentication,
                                            @PathVariable("id") String bookingId,
                                            @Valid @RequestBody AddRatingRequestDto dto) {
        String riderUserId = getUserId(authentication);
        log.info("Rider {} is leaving feedback for booking {}", riderUserId, bookingId);
        
        bookingService.addFeedback(bookingId, riderUserId, dto.getRating());
        return ResponseEntity.ok().build();
    }

    // --- DRIVER ENDPOINTS ---
    
    @GetMapping("/driver/bookings/me/history")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Page<BookingDto>> getMyDriverBookingHistory(
            Authentication authentication,
            @Valid @RequestParam(required = false) BookingFiltersDto bookingFiltersDto,
            @PageableDefault(size = 20, sort = "requestTime", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Driver {} fetching booking history", getUserId(authentication));
        return ResponseEntity.ok(bookingService.getBookingsForDriver(getUserId(authentication), bookingFiltersDto, pageable));
    }

    // Note: The "accept" endpoint is on the DRIVER-SERVICE, not here.
    
    @PostMapping("/driver/bookings/{id}/complete")
    @PreAuthorize("hasRole('DRIVER') and @securityService.isDriverOnBooking(authentication, #bookingId)")
    public ResponseEntity<BookingDto> completeRide(Authentication authentication, 
                                                   @PathVariable("id") String bookingId) {
        log.info("Driver {} completing booking {}", getUserId(authentication), bookingId);
        BookingDto booking = bookingService.completeRide(getUserId(authentication), bookingId);
        return ResponseEntity.ok(booking);
    }
    
    @PostMapping("/bookings/{id}/pay")
    @PreAuthorize("hasRole('RIDER') and @securityService.isRiderOnBooking(authentication, #id)")
    public ResponseEntity<BookingDto> payForBooking(Authentication authentication, 
                                                    @PathVariable("id") String id) {
        
        String riderUserId = getUserId(authentication);
        log.info("Rider {} is paying for booking {}", riderUserId, id);
        
        BookingDto paidBooking = bookingService.processPayment(id, riderUserId);
        return ResponseEntity.ok(paidBooking);
    }

    
    @GetMapping("/bookings/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isRiderOnBooking(authentication, #id) or @securityService.isDriverOnBooking(authentication, #id)")
    public ResponseEntity<BookingDto> getBookingDetails(Authentication authentication, 
                                                        @PathVariable("id") String id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    /**
     * --- NEW "CANCEL RIDE" ENDPOINT ---
     * Can be cancelled by the Rider, the Driver (if assigned), or an Admin.
     */
    @PostMapping("/bookings/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isRiderOnBooking(authentication, #id) or @securityService.isDriverOnBooking(authentication, #Id)")
    public ResponseEntity<BookingDto> cancelBooking(Authentication authentication, 
                                                    @PathVariable("id") String id) {
        
        String userId = getUserId(authentication);
        String userRole = getUserRole(authentication); // "RIDER", "DRIVER", "ADMIN"
        log.info("User {} ({}) is cancelling booking {}", userId, userRole, id);
        
        BookingDto cancelledBooking = bookingService.cancelBooking(id, userId, userRole);
        return ResponseEntity.ok(cancelledBooking);
    }
}