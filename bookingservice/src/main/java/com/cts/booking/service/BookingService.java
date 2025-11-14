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

import jakarta.persistence.EntityNotFoundException;
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

    @Transactional(readOnly = true)
    public EstimatedFareResponseDto estimateFare(String riderUserId, EstimateFareRequestDto dto) {
        UserDto rider = userClient.getUserById(riderUserId);

        // 1. Find the fare configuration
        FareConfig fareConfig = fareConfigRepository.findByCityAndVehicleType(rider.getCity(), dto.getVehicleType())
                .orElseThrow(() -> new BookingException("Service not available in " + rider.getCity() + " for " + dto.getVehicleType()));

        // 2. Calculate the fare using the private helper
        double distanceInKm = calculateHaversineDistance(dto.getPickupLat(), dto.getPickupLng(), dto.getDropoffLat(), dto.getDropoffLng());
        double totalFare = calculateFare(fareConfig, distanceInKm);
        double distanceFare = fareConfig.getRatePerKm() * distanceInKm;

        // 3. Build and return the response DTO
        return EstimatedFareResponseDto.builder()
                .city(rider.getCity())
                .vehicleType(dto.getVehicleType())
                .distanceInKm(distanceInKm)
                .baseFare(fareConfig.getBaseFare())
                .distanceFare(distanceFare)
                .totalFare(totalFare)
                .build();
    }

    // --- 1. REQUEST RIDE ---
    @Transactional
    public BookingDto requestRide(String riderUserId, CreateBookingRequestDto dto) {
        UserDto rider = userClient.getUserById(riderUserId); // Already fetching the rider DTO

        if(rider.getCity() == null || rider.getCity().isEmpty()) { // Added null check
            throw new BookingException("Please add city in your profile first.");
        } 
        
        FareConfig fareConfig = fareConfigRepository.findByCityAndVehicleType(rider.getCity(), dto.getVehicleType())
                .orElseThrow(() -> new BookingException("Service not available in " + rider.getCity()));
        
        if (getActiveBooking(riderUserId) != null) {
            throw new BookingException("You already have an active ride.");
        }

        double distanceInKm = calculateHaversineDistance(dto.getPickupLat(), dto.getPickupLng(), dto.getDropoffLat(), dto.getDropoffLng());
        double estimatedFare = calculateFare(fareConfig, distanceInKm);

        // --- MODIFIED CONSTRUCTOR ---
        // Pass the full 'rider' DTO to populate snapshot fields
        Booking booking = new Booking(riderUserId, rider, dto, rider.getCity(), estimatedFare);
        Booking savedBooking = bookingRepository.save(booking);
        
        // Send Kafka Event (Include Fare)
        RideRequestEventDto event = new RideRequestEventDto(
            savedBooking.getId(), savedBooking.getCity(), savedBooking.getVehicleType(),
            savedBooking.getFare(), savedBooking.getPickupLocationName(),
            savedBooking.getDropoffLocationName(), distanceInKm
        );
        kafkaTemplate.send(rideRequestsTopic, savedBooking.getId(), event);
        
        return bookingMapper.toDto(savedBooking);
    }

    public BookingDto getActiveRideForRider(String riderUserId) {
        Booking activeBooking = getActiveBooking(riderUserId);
        if(activeBooking == null) throw new ResourceNotFoundException("No active booking found.");
        return bookingMapper.toDto(activeBooking);
    }

    public BookingDto getActiveRideForDriver(String driverUserId) {
        List<BookingStatus> activeStatuses = List.of(BookingStatus.ACCEPTED, BookingStatus.IN_PROGRESS, BookingStatus.AWAITING_PAYMENT);
        Booking activeBooking = bookingRepository.findByDriverUserIdAndStatusIn(driverUserId, activeStatuses).orElse(null);
        if(activeBooking == null) throw new ResourceNotFoundException("No active booking found.");
        return bookingMapper.toDto(activeBooking);
    }

    public ResponseEntity<?> getVehicleAvailability(String riderId) {
        UserDto rider = userClient.getUserById(riderId);
        if(rider == null) throw new ResourceNotFoundException("Rider not found.");
        return driverClient.getAvailableVehiclesInCity(rider.getCity());
    }

    // --- 2. ASSIGN DRIVER (Called by Driver Service) ---
    @Transactional
    public void assignDriverToBooking(String bookingId, String driverUserId, String vehicleId) {
        Booking booking = findBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingException("Ride no longer available.");
        }
        
        // --- FETCH DRIVER PROFILE DATA ---
        UserDto driverProfile = null;
        try {
            // We assume UserClient returns all profile data (name, phone, rating, etc.)
            driverProfile = userClient.getUserById(driverUserId);
        } catch (Exception e) {
            log.warn("Could not fetch driver profile for booking {}: {}. Proceeding without profile snapshot.", bookingId, e.getMessage());
        }
        
        // --- SET DRIVER FIELDS ---
        booking.setDriverUserId(driverUserId);
        booking.setVehicleId(vehicleId);
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setAcceptedTime(LocalDateTime.now());
        
        // --- POPULATE SNAPSHOT FIELDS ---
        if (driverProfile != null) {
            booking.setDriverName(driverProfile.getFirstName() + " " + driverProfile.getLastName());
            booking.setDriverPhoneNumber(driverProfile.getPhoneNumber());
            List<BookingStatus> completedStatuses = List.of(BookingStatus.PAID, BookingStatus.COMPLETED);
            long totalTrips = bookingRepository.countByDriverUserIdAndStatusIn(driverUserId, completedStatuses);
            
            // ** IMPORTANT **
            // I am assuming your 'UserDto' has these getters based on your request.
            // If your DTO has different names (e.g., getOverallRating), 
            // you must change these lines.
            booking.setDriverProfileRating(driverProfile.getRating()); // Assuming getRating() returns a Double
            booking.setTotalTrips(totalTrips);     // Assuming getTotalTrips() returns an Integer
            booking.setDriverCreatedAt(driverProfile.getCreatedAt()); // Assuming getCreatedAt() returns LocalDateTime
        }
        
        Booking savedBooking = bookingRepository.save(booking);
        kafkaTemplate.send(bookingEventsTopic, savedBooking.getId(), BookingEventDto.fromBooking(savedBooking));
        log.info("Assigned driver {} to booking {}", driverUserId, bookingId);
    }

    @Transactional
    public BookingDto startRide(String bookingId, String driverUserId) {
        Booking booking = findBooking(bookingId);
        
        // 1. Verify the driver owns this ride
        if (!booking.getDriverUserId().equals(driverUserId)) {
            throw new AccessDeniedException("This is not your ride.");
        }

        // 2. Verify the ride is in the correct state
        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new BookingException("Ride cannot be started. Status is not ACCEPTED.");
        }

        // 3. Update the booking
        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setPickupTime(LocalDateTime.now());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // 4. Send Kafka event
        kafkaTemplate.send(bookingEventsTopic, savedBooking.getId(), BookingEventDto.fromBooking(savedBooking));
        
        log.info("Driver {} started ride for booking {}", driverUserId, bookingId);
        
        // 5. Return the updated DTO
        return bookingMapper.toDto(savedBooking);
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
    public void addFeedback(String bookingId, String riderUserId, AddRatingRequestDto dto) {
        Booking booking = findBooking(bookingId);
        if (!booking.getRiderUserId().equals(riderUserId)) throw new BookingException("Unauthorized.");
        if (booking.getStatus() != BookingStatus.PAID && booking.getStatus() != BookingStatus.COMPLETED) throw new BookingException("Ride not paid.");
        if (booking.getDriverRating() != null) throw new BookingException("Already rated.");

        if(dto == null) {
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
            return;
        }
        booking.setDriverRating(dto.getRating());
        if (booking.getStatus() == BookingStatus.PAID) booking.setStatus(BookingStatus.COMPLETED);
        
        bookingRepository.save(booking);
        try { 
            userClient.addRating(booking.getDriverUserId(), new AddRatingRequestDto(dto.getRating()));
        } catch (Exception e) {
            log.error("Rating sync failed", e);
        }
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
        String filterType = bookingFiltersDto != null ? bookingFiltersDto.getFilterType() : "";
        String searchTerm = bookingFiltersDto != null ? bookingFiltersDto.getSearchTerm() : "";
        
        return switch (filterType.toLowerCase()) {
            case "status" ->
                bookingRepository.findByRiderUserIdAndStatus(riderUserId, BookingStatus.valueOf(searchTerm.toUpperCase()), pageable)
                .map(bookingMapper::toDto);
            case "pickup_address" ->
                bookingRepository.findByRiderUserIdAndPickupLocationNameContainingIgnoreCase(riderUserId, searchTerm, pageable)
                .map(bookingMapper::toDto);
            case "dropoff_address" ->
                bookingRepository.findByRiderUserIdAndDropoffLocationNameContainingIgnoreCase(riderUserId, searchTerm, pageable)
                .map(bookingMapper::toDto);
            case "date" -> {
                LocalDate date = LocalDate.parse(searchTerm); // Expects "YYYY-MM-DD"
                yield bookingRepository.findByRiderUserIdAndRequestTimeBetween(riderUserId, date.atStartOfDay(), date.atTime(LocalTime.MAX), pageable)
                .map(bookingMapper::toDto);
            }
            default -> bookingRepository.findByRiderUserId(riderUserId, pageable).map(bookingMapper::toDto);
        };
    }

    @Transactional(readOnly = true)
    public Page<BookingDto> getBookingsForDriver(String driverUserId, BookingFiltersDto bookingFiltersDto, Pageable pageable) {
        String filterType = bookingFiltersDto != null ? bookingFiltersDto.getFilterType() : "";
        String searchTerm = bookingFiltersDto != null ? bookingFiltersDto.getSearchTerm() : "";
        
        return switch (filterType.toLowerCase()) {
            case "status" ->
                bookingRepository.findByDriverUserIdAndStatus(driverUserId, BookingStatus.valueOf(searchTerm.toUpperCase()), pageable)
                .map(bookingMapper::toDto);
            case "pickup_address" ->
                bookingRepository.findByDriverUserIdAndPickupLocationNameContainingIgnoreCase(driverUserId, searchTerm, pageable)
                .map(bookingMapper::toDto);
            case "dropoff_address" ->
                bookingRepository.findByDriverUserIdAndDropoffLocationNameContainingIgnoreCase(driverUserId, searchTerm, pageable)
                .map(bookingMapper::toDto);
            case "date" -> {
                LocalDate date = LocalDate.parse(searchTerm); // Expects "YYYY-MM-DD"
                yield bookingRepository.findByDriverUserIdAndRequestTimeBetween(driverUserId, date.atStartOfDay(), date.atTime(LocalTime.MAX), pageable)
                .map(bookingMapper::toDto);
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
        
        return fareConfigRepository.save(config);
    }

    @Transactional
    public FareConfig updateFare(String id, FareConfigDto fareDetails) {
        // 1. Find the existing fare config by its ID
        FareConfig existingConfig = fareConfigRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FareConfig not found with id: " + id));

        // 2. Update the fields from the incoming data
        existingConfig.setCity(fareDetails.getCity());
        existingConfig.setState(fareDetails.getState());
        existingConfig.setVehicleType(fareDetails.getVehicleType());
        existingConfig.setBaseFare(fareDetails.getBaseFare());
        existingConfig.setRatePerKm(fareDetails.getRatePerKm());
        
        // 3. Save the updated entity and return it
        return fareConfigRepository.save(existingConfig);
    }

    @Transactional
    public void deleteFare(String id) {
        // 1. Check if the entity exists before trying to delete it
        if (!fareConfigRepository.existsById(id)) {
            throw new EntityNotFoundException("FareConfig not found with id: " + id);
        }
        
        // 2. Delete the entity
        fareConfigRepository.deleteById(id);
    }

    // --- HELPER ---
    private Booking findBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
    }

    private Booking getActiveBooking(String riderUserId) {
        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.ACCEPTED, BookingStatus.IN_PROGRESS, BookingStatus.AWAITING_PAYMENT, BookingStatus.PAID);
        return bookingRepository.findByRiderUserIdAndStatusIn(riderUserId, activeStatuses).orElse(null);
    }

    private double calculateFare(FareConfig fareConfig, double distanceInKm) {
        if (distanceInKm < 0) {
            distanceInKm = 0; // Prevent negative fares
        }
        return fareConfig.getBaseFare() + (fareConfig.getRatePerKm() * distanceInKm);
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of Earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // distance in km
    }
}