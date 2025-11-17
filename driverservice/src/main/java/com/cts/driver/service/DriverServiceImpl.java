package com.cts.driver.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cts.driver.client.BookingClient;
import com.cts.driver.client.UserClient;
import com.cts.driver.dto.DriverProfileDto;
import com.cts.driver.dto.DriverStatsDto;
import com.cts.driver.dto.RideOfferDto;
import com.cts.driver.dto.UpdateDriverStatusDto;
import com.cts.driver.dto.UpdateVerificationDto;
import com.cts.driver.dto.client.BookingAssignmentDto;
import com.cts.driver.dto.client.UserDto;
import com.cts.driver.exception.OfferException;
import com.cts.driver.exception.ResourceNotFoundException;
import com.cts.driver.mapper.DriverMapper;
import com.cts.driver.mapper.RideOfferMapper;
import com.cts.driver.model.Driver;
import com.cts.driver.model.RideOffer;
import com.cts.driver.model.RideOfferStatus;
import com.cts.driver.model.Vehicle;
import com.cts.driver.model.VerificationType;
import com.cts.driver.repository.DriverRepository;
import com.cts.driver.repository.RideOfferRepository;
import com.cts.driver.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DriverServiceImpl {

    @Autowired private DriverRepository driverRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private RideOfferRepository rideOfferRepository;
    @Autowired private UserClient userClient;
    @Autowired private BookingClient bookingClient;
    @Autowired private DriverMapper driverMapper;
    @Autowired private RideOfferMapper rideOfferMapper;

    @Transactional(readOnly = true)
    public DriverProfileDto getDriverProfileByUserId(String userId) {
        log.info("Fetching profile for user ID: {}", userId);
        Driver driver = findDriverByUserId(userId);
        
        log.debug("Calling User-Service (Feign) to get user data for {}", userId);
        UserDto user = userClient.getUserById(userId);
        
        return driverMapper.toDriverProfileDto(driver, user);
    }

    @Transactional
    public DriverProfileDto updateVerification(String userId, UpdateVerificationDto dto) {
        log.info("Updating verification info for user ID: {}", userId);
        Driver driver = findDriverByUserId(userId);
        driver.setAadharNumber(dto.getAadharNumber());
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setDriverLicenseExpiry(dto.getDriverLicenseExpiry());
        
        driver.setAadhaarVerified(false);
        driver.setPanVerified(false);
        driver.setDriverLicenseVerified(false);
        
        Driver updatedDriver = driverRepository.save(driver);
        UserDto user = userClient.getUserById(userId);
        return driverMapper.toDriverProfileDto(updatedDriver, user);
    }

    @Transactional
    public boolean updateDriverStatus(String userId, UpdateDriverStatusDto dto) {
        log.info("Updating current city for driver ID: {}", userId);
        Driver driver = findDriverByUserId(userId);
        driver.setAvailable(dto.isAvailable());
        driver.setCurrentCity(dto.getCurrentCity());
        driverRepository.save(driver);
        return driver.isAvailable();
    }

    @Transactional
    public boolean updateDriverStatus(String userId, boolean available) {
        log.info("Updating status for driver ID: {}", userId);
        Driver driver = findDriverByUserId(userId);
        driver.setAvailable(available);
        driverRepository.save(driver);
        return driver.isAvailable();
    }
    
    @Transactional(readOnly = true)
    public List<RideOfferDto> getPendingOffers(String userId) {
        Driver driver = findDriverByUserId(userId);
        log.debug("Fetching pending offers for driver ID: {}", driver.getId());
        List<RideOffer> offers = rideOfferRepository.findByDriverIdAndStatus(driver.getId(), RideOfferStatus.PENDING);
        
        return offers.stream()
                .map(rideOfferMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptOffer(String userId, String offerId, String selectedVehicleId) {
        log.info("Driver (User ID: {}) attempting to accept offer ID: {} with Vehicle: {}", userId, offerId, selectedVehicleId);
        Driver driver = findDriverByUserId(userId);
        
        RideOffer offer = rideOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found or expired."));

        if (!offer.getDriverId().equals(driver.getId())) {
            throw new OfferException("This offer does not belong to you.");
        }
        if (offer.getStatus() != RideOfferStatus.PENDING) {
            throw new OfferException("Offer is no longer available.");
        }

        Vehicle selectedVehicle = vehicleRepository.findById(selectedVehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found."));

        boolean ownsVehicle = driver.getVehicles().stream()
                .anyMatch(v -> v.getId().equals(selectedVehicleId));
        if (!ownsVehicle) {
            throw new OfferException("You do not own this vehicle.");
        }

        if (!selectedVehicle.isVerified()) {
            throw new OfferException("Vehicle is not verified.");
        }

        if (!selectedVehicle.getVehicleType().name().equalsIgnoreCase(offer.getVehicleCategory())) {
            throw new OfferException("Vehicle Type Mismatch. Required: " + offer.getVehicleCategory());
        }
        
        offer.setStatus(RideOfferStatus.ACCEPTED);
        rideOfferRepository.save(offer);

        List<RideOffer> otherOffers = rideOfferRepository.findByBookingIdAndStatus(offer.getBookingId(), RideOfferStatus.PENDING);
        otherOffers.forEach(o -> o.setStatus(RideOfferStatus.REJECTED));
        rideOfferRepository.saveAll(otherOffers);

        driver.setAvailable(false);
        driverRepository.save(driver);

        try {
            log.info("Assigning booking {} to driver {}", offer.getBookingId(), driver.getUserId());
            bookingClient.assignBooking(offer.getBookingId(), 
                new BookingAssignmentDto(driver.getUserId(), selectedVehicleId));
        } catch (Exception e) {
            log.error("Failed to assign booking via Feign. Rolling back.", e);
            throw new RuntimeException("Failed to confirm booking.", e);
        }
    }
    
    @Transactional(readOnly = true)
    public Page<DriverProfileDto> getAllDrivers(Pageable pageable, String filterType, String searchContent) {
        System.out.println(filterType + " " + searchContent);
        Page<Driver> drivers = null;
        if (filterType != null && searchContent != null && !searchContent.isEmpty()) {
            if ("AADHAR".equalsIgnoreCase(filterType))
                drivers = driverRepository.findByAadharNumberContains(searchContent, pageable);
            else if ("LICENSE".equalsIgnoreCase(filterType))
                drivers = driverRepository.findByLicenseNumberContains(searchContent, pageable);
        } else drivers = driverRepository.findAll(pageable);
        return drivers.map(driver -> {
            UserDto user = userClient.getUserById(driver.getUserId());
            return driverMapper.toDriverProfileDto(driver, user);
        });
    }

    public Page<DriverProfileDto> getVerificationQueue(Pageable pageable) {
        return driverRepository.findDriversForVerification(pageable).map(driver -> {
            return driverMapper.toDriverProfileDto(driver);
        });
    }
    
    @Transactional
    public DriverProfileDto approveVerification(String driverId, VerificationType type) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found."));

        switch (type) {
            case PAN -> driver.setPanVerified(true);
            case AADHAAR -> driver.setAadhaarVerified(true);
            case LICENSE -> driver.setDriverLicenseVerified(true);
        }
        log.info("Approved {} for driver {}", type, driverId);
        if(driver.isAadhaarVerified() && driver.isDriverLicenseVerified()) driver.setAvailable(true);
        Driver savedDriver = driverRepository.save(driver);
        UserDto user = userClient.getUserById(savedDriver.getUserId());
        return driverMapper.toDriverProfileDto(savedDriver, user);
    }
    
    public DriverStatsDto getDriversAndVehiclesCount() {
        return new DriverStatsDto(driverRepository.count(), vehicleRepository.count());
    }
    
    @Transactional
    public void approveVehicle(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
        
        vehicle.setVerified(true);
        vehicle.setPucVerified(true);
        vehicle.setInsuranceVerified(true);
        vehicle.setRcVerified(true);
        vehicleRepository.save(vehicle);
    }

    // Helper method
    public Driver findDriverByUserId(String userId) {
        return driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId));
    }
}