package com.cts.booking.repository;

import com.cts.booking.model.Booking;
import com.cts.booking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    
    Page<Booking> findByRiderUserId(String riderUserId, Pageable pageable);
    Page<Booking> findByRiderUserIdAndStatus(String riderUserId, BookingStatus bookingStatus, Pageable pageable);
    Page<Booking> findByRiderUserIdAndPickupLocationNameContainingIgnoreCase(String riderUserId, String searchTerm, Pageable pageable);
    Page<Booking> findByRiderUserIdAndDropoffLocationNameContainingIgnoreCase(String riderUserId, String searchTerm, Pageable pageable);
    Page<Booking> findByRiderUserIdAndRequestTimeBetween(String riderUserId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    Page<Booking> findByDriverUserId(String driverUserId, Pageable pageable);
    Page<Booking> findByDriverUserIdAndStatus(String driverUserId, BookingStatus bookingStatus, Pageable pageable);
    Page<Booking> findByDriverUserIdAndPickupLocationNameContainingIgnoreCase(String driverUserId, String searchTerm, Pageable pageable);
    Page<Booking> findByDriverUserIdAndDropoffLocationNameContainingIgnoreCase(String driverUserId, String searchTerm, Pageable pageable);
    Page<Booking> findByDriverUserIdAndRequestTimeBetween(String driverUserId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Optional<Booking> findByRiderUserIdAndStatusIn(String riderUserId, List<BookingStatus> activeStatuses);
    Optional<Booking> findByDriverUserIdAndStatusIn(String driverUserId, List<BookingStatus> activeStatuses);

    long countByDriverUserIdAndStatusIn(String driverUserId, List<BookingStatus> statuses);
}