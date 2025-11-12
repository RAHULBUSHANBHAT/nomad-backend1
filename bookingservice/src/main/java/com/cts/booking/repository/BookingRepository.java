package com.cts.booking.repository;

import com.cts.booking.model.Booking;
import com.cts.booking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List; // <-- ADD
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    
    // For Rider's "My Bookings" page
    Page<Booking> findByRiderUserId(String riderUserId, Pageable pageable);
    Page<Booking> findByRiderIdAndBookingStatus(String riderUserId, BookingStatus bookingStatus, Pageable pageable);
    Page<Booking> findByRiderIdAndPickupLocationNameContainingIgnoreCase(String riderUserId, String searchTerm, Pageable pageable);
    Page<Booking> findByRiderIdAndDropoffLocationNameContainingIgnoreCase(String riderUserId, String searchTerm, Pageable pageable);
    Page<Booking> findByRiderIdAndCreatedAtBetween(String riderUserId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // For Driver's "My Bookings" page
    Page<Booking> findByDriverUserId(String driverUserId, Pageable pageable);
    Page<Booking> findByDriverIdAndBookingStatus(String driverUserId, BookingStatus bookingStatus, Pageable pageable);
    Page<Booking> findByDriverIdAndPickupLocationNameContainingIgnoreCase(String driverUserId, String searchTerm, Pageable pageable);
    Page<Booking> findByDriverIdAndDropoffLocationNameContainingIgnoreCase(String driverUserId, String searchTerm, Pageable pageable);
    Page<Booking> findByDriverIdAndCreatedAtBetween(String driverUserId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Optional<Booking> findByRiderUserIdAndStatusIn(String riderUserId, List<BookingStatus> activeStatuses);
}