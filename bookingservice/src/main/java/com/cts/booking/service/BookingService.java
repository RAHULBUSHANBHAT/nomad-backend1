package com.cts.booking.service;

import com.cts.booking.client.DriverClient;
import com.cts.booking.client.UserClient; 
import com.cts.booking.client.WalletClient;
import com.cts.booking.dto.AddRatingRequestDto; 
import com.cts.booking.dto.BookingDto;
import com.cts.booking.dto.CreateBookingRequestDto;
import com.cts.booking.dto.FareConfigDto;
import com.cts.booking.dto.client.RidePaymentRequestDto;
import com.cts.booking.dto.kafka.BookingEventDto;
import com.cts.booking.dto.kafka.RideRequestEventDto;
import com.cts.booking.exception.BookingException;
import com.cts.booking.exception.ResourceNotFoundException;
import com.cts.booking.mapper.BookingMapper;
import com.cts.booking.model.Booking;
import com.cts.booking.model.BookingStatus;
import com.cts.booking.model.FareConfig;
import com.cts.booking.repository.BookingRepository;
import com.cts.booking.repository.FareConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cts.booking.model.BookingStatus;
import java.util.List;

import java.time.LocalDateTime;


@Service
@Slf4j
public class BookingService {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private FareConfigRepository fareConfigRepository;
    @Autowired private BookingMapper bookingMapper;
    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired private WalletClient walletClient;
    @Autowired private DriverClient driverClient;
    @Autowired private UserClient userClient; // <-- ADDED FOR FEEDBACK

    @Value("${app.kafka.topics.ride-requests}")
    private String rideRequestsTopic;
    @Value("${app.kafka.topics.booking-events}")
    private String bookingEventsTopic;
    @Value("${app.commission-rate}")
    private double commissionRate;

    /**
     * Flow 1: Rider requests a new ride.
     */
    @Transactional
    public BookingDto requestRide(String riderUserId, CreateBookingRequestDto dto) {
        log.info("Rider {} requested a ride in {}", riderUserId, dto.getCity());
        
        FareConfig fareConfig = fareConfigRepository.findByCityAndVehicleType(dto.getCity(), dto.getVehicleType())
                .orElseThrow(() -> new BookingException("Sorry, no " + dto.getVehicleType() + " service available in " + dto.getCity()));
        
                List<BookingStatus> activeStatuses = List.of(
            BookingStatus.PENDING, 
            BookingStatus.ACCEPTED, 
            BookingStatus.IN_PROGRESS
        );
        if (bookingRepository.findByRiderUserIdAndStatusIn(riderUserId, activeStatuses).isPresent()) {
            throw new BookingException("You already have an active ride. You cannot book another.");
        }

        // Mock fare calculation
        double estimatedFare = fareConfig.getBaseFare() + (fareConfig.getRatePerKm() * 10.0); 

        Booking booking = new Booking(riderUserId, dto, estimatedFare);
        Booking savedBooking = bookingRepository.save(booking);
        
        RideRequestEventDto event = new RideRequestEventDto(
            savedBooking.getId(),
            savedBooking.getCity(),
            savedBooking.getVehicleType()
        );
        kafkaTemplate.send(rideRequestsTopic, savedBooking.getId(), event);
        log.info("Sent ride request to Kafka for booking ID: {}", savedBooking.getId());
        
        return bookingMapper.toDto(savedBooking);
    }
    
    /**
     * Flow 2: Driver completes a ride (The Payment Flow)
     */
   @Transactional
    public BookingDto completeRide(String driverUserId, String bookingId) {
        log.info("Driver {} is completing booking {}", driverUserId, bookingId);
        Booking booking = findBooking(bookingId);

        if (!booking.getDriverUserId().equals(driverUserId)) {
            throw new BookingException("You are not the driver for this ride.");
        }
        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            // (We'll assume for now a ride must be IN_PROGRESS to be completed)
            throw new BookingException("This ride must be IN_PROGRESS to be completed.");
        }

        // --- REFACTORED LOGIC ---
        // We NO LONGER call wallet-service here.
        // We just set the status to COMPLETED and payment to UNPAID.
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedTime(LocalDateTime.now());
        booking.setPaymentStatus("UNPAID"); // Was "PAYMENT_FAILED"
        
        Booking savedBooking = bookingRepository.save(booking);

        // We DO NOT set the driver to available. They are not free until they are paid.

        // Send Kafka event
        kafkaTemplate.send(bookingEventsTopic, savedBooking.getId(), BookingEventDto.fromBooking(savedBooking));
        log.info("Booking {} marked as COMPLETED. Awaiting payment.", bookingId);
        return bookingMapper.toDto(savedBooking);
    }

    @Transactional
    public BookingDto processPayment(String bookingId, String riderUserId) {
        log.info("Rider {} is now paying for booking {}", riderUserId, bookingId);
        Booking booking = findBooking(bookingId);
        
        // 1. Security & State Checks
        if (!booking.getRiderUserId().equals(riderUserId)) {
            throw new AccessDeniedException("You are not the rider for this booking.");
        }
        if (booking.getStatus() == BookingStatus.PAID) {
            throw new BookingException("This booking has already been paid.");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BookingException("This booking is not yet completed.");
        }
        
        // 2. --- This is the logic we MOVED from completeRide ---
        double fare = booking.getFare();
        double commission = fare * commissionRate;
        
        RidePaymentRequestDto paymentDto = new RidePaymentRequestDto();
        paymentDto.setBookingId(bookingId);
        paymentDto.setRiderUserId(booking.getRiderUserId());
        paymentDto.setDriverUserId(booking.getDriverUserId());
        paymentDto.setFare(fare);
        paymentDto.setCommission(commission);

        // 3. Call the wallet-service (System-to-System call)
        try {
            log.info("Calling Wallet-Service to execute payment for booking {}", bookingId);
            walletClient.executeRidePayment(paymentDto);
            booking.setStatus(BookingStatus.PAID);
            booking.setPaymentStatus("PAID");
        } catch (Exception e) {
            log.error("CRITICAL: Payment failed for booking {}. Error: {}", bookingId, e.getMessage());
            booking.setPaymentStatus("PAYMENT_FAILED");
            // Re-throw the exception so the user knows it failed
            throw new BookingException("Payment failed: " + e.getMessage());
        }

        Booking savedBooking = bookingRepository.save(booking);

        // 4. NOW we can set the driver to available.
        try {
            driverClient.setAvailability(booking.getDriverUserId(), true);
        } catch (Exception e) {
            log.error("Failed to set driver {} back to available. Error: {}", booking.getDriverUserId(), e.getMessage());
        }
        
        kafkaTemplate.send(bookingEventsTopic, savedBooking.getId(), BookingEventDto.fromBooking(savedBooking));
        return bookingMapper.toDto(savedBooking);
    }
    
    /**
     * --- THIS IS THE NEW "CANCEL RIDE" METHOD ---
     */
    @Transactional
    public BookingDto cancelBooking(String bookingId, String userId, String userRole) {
        log.info("User {} (Role: {}) attempting to cancel booking {}", userId, userRole, bookingId);
        Booking booking = findBooking(bookingId);

        // 1. Check if the booking can be cancelled
        if (booking.getStatus() == BookingStatus.COMPLETED || 
            booking.getStatus() == BookingStatus.PAID || 
            booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("This ride is already finished or cancelled and cannot be updated.");
        }

        // 2. Check if the driver was already assigned
        boolean wasDriverAssigned = (booking.getDriverUserId() != null && 
                                     (booking.getStatus() == BookingStatus.ACCEPTED || booking.getStatus() == BookingStatus.IN_PROGRESS));
        String assignedDriverId = booking.getDriverUserId();

        // 3. Update the booking status
        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);
        
        // 4. Send a Kafka event
        kafkaTemplate.send(bookingEventsTopic, savedBooking.getId(), BookingEventDto.fromBooking(savedBooking));
        log.info("Booking {} has been cancelled.", bookingId);

        // 5. CRITICAL: If a driver was on this ride, set them back to 'available'
        if (wasDriverAssigned) {
            try {
                log.info("Setting driver {} back to available after cancellation.", assignedDriverId);
                driverClient.setAvailability(assignedDriverId, true);
            } catch (Exception e) {
                log.error("Failed to set driver {} back to available. Error: {}", assignedDriverId, e.getMessage());
            }
        }
        
        return bookingMapper.toDto(savedBooking);
    }
    
    /**
     * --- THIS IS THE NEW "ADD FEEDBACK" METHOD ---
     */
    @Transactional
    public void addFeedback(String bookingId, String riderUserId, int rating) {
        log.info("Rider {} is leaving a rating of {} for booking {}", riderUserId, rating, bookingId);
        Booking booking = findBooking(bookingId);

        if (!booking.getRiderUserId().equals(riderUserId)) {
            throw new BookingException("You are not the rider for this booking.");
        }
        
        if (booking.getStatus() != BookingStatus.PAID && booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BookingException("You can only leave feedback for a completed ride.");
        }

        if (booking.getDriverUserId() == null) {
            throw new BookingException("This ride had no driver, cannot leave feedback.");
        }
        
        if(booking.getDriverRating() != null) {
            throw new BookingException("Feedback has already been submitted for this ride.");
        }
        
        booking.setDriverRating(rating);
        bookingRepository.save(booking);

        try {
            log.info("Calling User-Service to add rating for driver (user ID: {})", booking.getDriverUserId());
            userClient.addRating(booking.getDriverUserId(), new AddRatingRequestDto(rating));
        } catch (Exception e) {
            log.error("Failed to add rating to user-service for user {}. Error: {}", booking.getDriverUserId(), e.getMessage());
            throw new RuntimeException("Rating could not be submitted to user service.", e);
        }
    }
    
    // --- Other Methods ---
    @Transactional(readOnly = true)
    public Page<BookingDto> getBookingsForRider(String riderUserId, Pageable pageable) {
        return bookingRepository.findByRiderUserId(riderUserId, pageable)
                .map(bookingMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<BookingDto> getBookingsForDriver(String driverUserId, Pageable pageable) {
        return bookingRepository.findByDriverUserId(driverUserId, pageable)
                .map(bookingMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public BookingDto getBookingById(String bookingId) {
        return bookingMapper.toDto(findBooking(bookingId));
    }
    
    // --- Internal Methods ---
    @Transactional
    public void assignDriverToBooking(String bookingId, String driverUserId) {
        Booking booking = findBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingException("This ride is no longer available.");
        }
        booking.setDriverUserId(driverUserId);
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setAcceptedTime(LocalDateTime.now());
        bookingRepository.save(booking);
        log.info("Internal: Assigned driver {} to booking {}", driverUserId, bookingId);
    }

    // --- Admin Methods ---
    @Transactional(readOnly = true)
    public List<FareConfig> getAllFares() {
        return fareConfigRepository.findAll();
    }

    @Transactional
    public FareConfig setFare(FareConfigDto dto) {
        FareConfig config = fareConfigRepository.findByCityAndVehicleType(dto.getCity(), dto.getVehicleType())
                .orElse(new FareConfig()); // Create new if not found
        
        config.setCity(dto.getCity());
        config.setState(dto.getState());
        config.setVehicleType(dto.getVehicleType());
        config.setBaseFare(dto.getBaseFare());
        config.setRatePerKm(dto.getRatePerKm());
        config.setRatePerMinute(dto.getRatePerMinute());
        
        return fareConfigRepository.save(config);
    }

    @Transactional(readOnly = true)
    public Page<BookingDto> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable)
                .map(bookingMapper::toDto);
    }

    // Helper
    private Booking findBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
    }
}