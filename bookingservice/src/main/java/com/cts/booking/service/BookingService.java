package com.cts.booking.service;

import com.cts.booking.client.*;
import com.cts.booking.dto.*;
import com.cts.booking.dto.client.RidePaymentRequestDto;
import com.cts.booking.dto.client.UserDto;
import com.cts.booking.dto.kafka.*;
import com.cts.booking.exception.*;
import com.cts.booking.mapper.BookingMapper;
import com.cts.booking.model.*;
import com.cts.booking.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
public class BookingService {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private FareConfigRepository fareConfigRepository;
    @Autowired private BookingMapper bookingMapper;
    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired private WalletClient walletClient;
    @Autowired private DriverClient driverClient;
    @Autowired private UserClient userClient;

    @Value("${app.kafka.topics.ride-requests}") private String rideRequestsTopic;
    @Value("${app.kafka.topics.booking-events}") private String bookingEventsTopic;
    @Value("${app.commission-rate}") private double commissionRate;

    // --- 1. REQUEST RIDE ---
    @Transactional
    public BookingDto requestRide(String riderUserId, CreateBookingRequestDto dto) {
        FareConfig fareConfig = fareConfigRepository.findByCityAndVehicleType(dto.getCity(), dto.getVehicleType())
                .orElseThrow(() -> new BookingException("Service not available in " + dto.getCity()));
        
        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.ACCEPTED, BookingStatus.IN_PROGRESS, BookingStatus.AWAITING_PAYMENT);
        if (bookingRepository.findByRiderUserIdAndStatusIn(riderUserId, activeStatuses).isPresent()) {
            throw new BookingException("You already have an active ride.");
        }

        double estimatedFare = fareConfig.getBaseFare() + (fareConfig.getRatePerKm() * 10.0); 

        Booking booking = new Booking(riderUserId, dto, estimatedFare);
        Booking savedBooking = bookingRepository.save(booking);
        
        // Send Kafka Event (Include Fare)
        RideRequestEventDto event = new RideRequestEventDto(
            savedBooking.getId(), savedBooking.getCity(), savedBooking.getVehicleType(), savedBooking.getFare()
        );
        kafkaTemplate.send(rideRequestsTopic, savedBooking.getId(), event);
        
        return bookingMapper.toDto(savedBooking);
    }

    public ResponseEntity<?> getVehicleAvailability(String city) {
        return driverClient.getAvailableVehiclesInCity(city);
    }

    // --- 2. ASSIGN DRIVER (Called by Driver Service) ---
    @Transactional
    public void assignDriverToBooking(String bookingId, String driverUserId, String vehicleId) {
        Booking booking = findBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingException("Ride no longer available.");
        }
        
        booking.setDriverUserId(driverUserId);
        booking.setVehicleId(vehicleId);
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setAcceptedTime(LocalDateTime.now());
        
        Booking savedBooking = bookingRepository.save(booking);
        kafkaTemplate.send(bookingEventsTopic, savedBooking.getId(), BookingEventDto.fromBooking(savedBooking));
        log.info("Assigned driver {} to booking {}", driverUserId, bookingId);
    }

    // --- 3. COMPLETE RIDE ---
     @Transactional
    public BookingDto completeRide(String driverUserId, String bookingId) {
        Booking booking = findBooking(bookingId);
        if (!booking.getDriverUserId().equals(driverUserId)) throw new BookingException("Not your ride.");
        if (booking.getStatus() != BookingStatus.IN_PROGRESS) throw new BookingException("Ride must be IN_PROGRESS.");

        booking.setStatus(BookingStatus.AWAITING_PAYMENT);
        booking.setCompletedTime(LocalDateTime.now());
        
        Booking savedBooking = bookingRepository.save(booking);
        kafkaTemplate.send(bookingEventsTopic, savedBooking.getId(), BookingEventDto.fromBooking(savedBooking));
        return bookingMapper.toDto(savedBooking);
    }

    // --- 4. WALLET PAYMENT (Populates Data + Sync Call) ---
    @Transactional
    public BookingDto processPayment(String bookingId, String riderUserId) {
        Booking booking = findBooking(bookingId);
        if (!booking.getRiderUserId().equals(riderUserId)) throw new AccessDeniedException("Unauthorized.");
        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT) throw new BookingException("Not awaiting payment.");

        // Build the detailed DTO
        RidePaymentRequestDto requestDto = buildPaymentRequest(booking, "WALLET");

        try {
            walletClient.executeRidePayment(requestDto);
            
            booking.setStatus(BookingStatus.PAID);
            booking.setPaymentStatus("PAID");
            try { driverClient.setAvailability(booking.getDriverUserId(), true); } catch (Exception e) { log.error("Failed to free driver", e); }

        } catch (Exception e) {
            booking.setPaymentStatus("PAYMENT_FAILED");
            throw new BookingException("Payment failed: " + e.getMessage());
        }

        Booking savedBooking = bookingRepository.save(booking);
        kafkaTemplate.send(bookingEventsTopic, savedBooking.getId(), BookingEventDto.fromBooking(savedBooking));
        return bookingMapper.toDto(savedBooking);
    }

    // --- 5. CASH PAYMENT (Populates Data + Sync Call) ---
    @Transactional
    public BookingDto confirmCashPayment(String bookingId, String driverUserId) {
        Booking booking = findBooking(bookingId);
        if (!booking.getDriverUserId().equals(driverUserId)) throw new AccessDeniedException("Unauthorized.");
        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT) throw new BookingException("Not awaiting payment.");

        // Build the detailed DTO
        RidePaymentRequestDto requestDto = buildPaymentRequest(booking, "CASH");

        try {
            walletClient.executeCashPayment(requestDto);
            
            booking.setStatus(BookingStatus.PAID);
            booking.setPaymentStatus("PAID_CASH");
            try { driverClient.setAvailability(driverUserId, true); } catch (Exception e) { log.error("Failed to free driver", e); }

        } catch (Exception e) {
            throw new BookingException("Commission deduction failed: " + e.getMessage());
        }

        Booking savedBooking = bookingRepository.save(booking);
        kafkaTemplate.send(bookingEventsTopic, savedBooking.getId(), BookingEventDto.fromBooking(savedBooking));
        return bookingMapper.toDto(savedBooking);
    }

    // --- HELPER: BUILDS THE DTO FOR WALLET ---
    private RidePaymentRequestDto buildPaymentRequest(Booking booking, String mode) {
        double totalFare = booking.getFare();
        double taxes = totalFare * 0.05;
        double commission = totalFare * commissionRate;
        double distanceFare = totalFare * 0.75;
        double baseFare = totalFare - (taxes + distanceFare); 

        // Fetch Names/Phones (Handle failures gracefully)
        UserDto rider = new UserDto(); 
        UserDto driver = new UserDto();
        try {
            rider = userClient.getUserById(booking.getRiderUserId());
            driver = userClient.getUserById(booking.getDriverUserId());
        } catch (Exception e) {
            log.warn("Could not fetch user details: {}", e.getMessage());
            rider.setFirstName("Unknown"); rider.setLastName(""); rider.setPhoneNumber("N/A");
            driver.setFirstName("Unknown"); driver.setLastName(""); driver.setPhoneNumber("N/A");
        }

        return RidePaymentRequestDto.builder()
                .bookingId(booking.getId())
                .riderUserId(booking.getRiderUserId())
                .riderName(rider.getFirstName() + " " + rider.getLastName())
                .riderPhone(rider.getPhoneNumber())
                .driverUserId(booking.getDriverUserId())
                .driverName(driver.getFirstName() + " " + driver.getLastName())
                .driverPhone(driver.getPhoneNumber())
                .totalFare(totalFare)
                .commissionFee(commission)
                .baseFare(baseFare)
                .distanceFare(distanceFare)
                .taxes(taxes)
                .pickupAddress(booking.getPickupLocationName())
                .dropoffAddress(booking.getDropoffLocationName())
                .paymentMode(mode)
                .build();
    }
    
    // --- 6. FEEDBACK ---
    @Transactional
    public void addFeedback(String bookingId, String riderUserId, int rating) {
        Booking booking = findBooking(bookingId);
        if (!booking.getRiderUserId().equals(riderUserId)) throw new BookingException("Unauthorized.");
        if (booking.getStatus() != BookingStatus.PAID && booking.getStatus() != BookingStatus.COMPLETED) throw new BookingException("Ride not paid.");
        if (booking.getDriverRating() != null) throw new BookingException("Already rated.");

        booking.setDriverRating(rating);
        if (booking.getStatus() == BookingStatus.PAID) booking.setStatus(BookingStatus.COMPLETED);
        
        bookingRepository.save(booking);
        try { userClient.addRating(booking.getDriverUserId(), new AddRatingRequestDto(rating)); } catch (Exception e) { log.error("Rating sync failed", e); }
    }
    
    // --- 7. CANCELLATION ---
    @Transactional
    public BookingDto cancelBooking(String bookingId, String userId, String userRole) {
         Booking booking = findBooking(bookingId);
         
         if (booking.getStatus() == BookingStatus.COMPLETED || 
             booking.getStatus() == BookingStatus.PAID || 
             booking.getStatus() == BookingStatus.AWAITING_PAYMENT) {
             throw new BookingException("Ride is too far along to cancel.");
         }
         
         boolean wasDriverAssigned = (booking.getDriverUserId() != null);
         String driverId = booking.getDriverUserId();
         
         booking.setStatus(BookingStatus.CANCELLED);
         Booking saved = bookingRepository.save(booking);
         
         if(wasDriverAssigned) {
             try { driverClient.setAvailability(driverId, true); } catch(Exception e) {}
         }
         
         kafkaTemplate.send(bookingEventsTopic, saved.getId(), BookingEventDto.fromBooking(saved));
         return bookingMapper.toDto(saved);
    }

    // --- 8. VIEW METHODS (Filters) ---
    @Transactional(readOnly = true)
    public Page<BookingDto> getBookingsForRider(String riderUserId, BookingFiltersDto bookingFiltersDto, Pageable pageable) {
        String searchTerm = bookingFiltersDto.getSearchTerm();
        String filterType = bookingFiltersDto.getFilterType() != null ? bookingFiltersDto.getFilterType() : "";
        
        return switch (filterType.toLowerCase()) {
            case "status" -> bookingRepository.findByRiderIdAndBookingStatus(riderUserId, BookingStatus.valueOf(searchTerm.toUpperCase()), pageable).map(bookingMapper::toDto);
            case "pickup_address" -> bookingRepository.findByRiderIdAndPickupLocationNameContainingIgnoreCase(riderUserId, searchTerm, pageable).map(bookingMapper::toDto);
            case "dropoff_address" -> bookingRepository.findByRiderIdAndDropoffLocationNameContainingIgnoreCase(riderUserId, searchTerm, pageable).map(bookingMapper::toDto);
            case "date" -> {
                LocalDate date = LocalDate.parse(searchTerm);
                yield bookingRepository.findByRiderIdAndCreatedAtBetween(riderUserId, date.atStartOfDay(), date.atTime(LocalTime.MAX), pageable).map(bookingMapper::toDto);
            }
            default -> bookingRepository.findByRiderUserId(riderUserId, pageable).map(bookingMapper::toDto);
        };
    }

    @Transactional(readOnly = true)
    public Page<BookingDto> getBookingsForDriver(String driverUserId, BookingFiltersDto bookingFiltersDto, Pageable pageable) {
        String searchTerm = bookingFiltersDto.getSearchTerm();
        String filterType = bookingFiltersDto.getFilterType() != null ? bookingFiltersDto.getFilterType() : "";
        
        return switch (filterType.toLowerCase()) {
            case "status" -> bookingRepository.findByDriverIdAndBookingStatus(driverUserId, BookingStatus.valueOf(searchTerm.toUpperCase()), pageable).map(bookingMapper::toDto);
            case "pickup_address" -> bookingRepository.findByDriverIdAndPickupLocationNameContainingIgnoreCase(driverUserId, searchTerm, pageable).map(bookingMapper::toDto);
            case "dropoff_address" -> bookingRepository.findByDriverIdAndDropoffLocationNameContainingIgnoreCase(driverUserId, searchTerm, pageable).map(bookingMapper::toDto);
            case "date" -> {
                LocalDate date = LocalDate.parse(searchTerm);
                yield bookingRepository.findByDriverIdAndCreatedAtBetween(driverUserId, date.atStartOfDay(), date.atTime(LocalTime.MAX), pageable).map(bookingMapper::toDto);
            }
            default -> bookingRepository.findByDriverUserId(driverUserId, pageable).map(bookingMapper::toDto);
        };
    }
    
    @Transactional(readOnly = true)
    public BookingDto getBookingById(String bookingId) {
        return bookingMapper.toDto(findBooking(bookingId));
    }
    
    @Transactional(readOnly = true)
    public Page<BookingDto> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(bookingMapper::toDto);
    }

    // --- 9. ADMIN FARE METHODS ---
    @Transactional(readOnly = true)
    public List<FareConfig> getAllFares() {
        return fareConfigRepository.findAll();
    }

    @Transactional
    public FareConfig setFare(FareConfigDto dto) {
        FareConfig config = fareConfigRepository.findByCityAndVehicleType(dto.getCity(), dto.getVehicleType())
                .orElse(new FareConfig()); 
        
        config.setCity(dto.getCity());
        config.setState(dto.getState());
        config.setVehicleType(dto.getVehicleType());
        config.setBaseFare(dto.getBaseFare());
        config.setRatePerKm(dto.getRatePerKm());
        config.setRatePerMinute(dto.getRatePerMinute());
        
        return fareConfigRepository.save(config);
    }

    // --- HELPER ---
    private Booking findBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
    }
}