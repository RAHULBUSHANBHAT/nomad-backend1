package com.cts.booking.repository;

import com.cts.booking.model.Booking;
import com.cts.booking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cts.booking.model.BookingStatus;
import java.util.List; // <-- ADD
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    
    // For Rider's "My Bookings" page
    Page<Booking> findByRiderUserId(String riderUserId, Pageable pageable);
    
    // For Driver's "My Bookings" page
    Page<Booking> findByDriverUserId(String driverUserId, Pageable pageable);
    Optional<Booking> findByRiderUserIdAndStatusIn(String riderUserId, List<BookingStatus> activeStatuses);
}