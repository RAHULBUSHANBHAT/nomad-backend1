package com.cts.driver.service;

import com.cts.driver.client.BookingClient;
import com.cts.driver.client.UserClient;
import com.cts.driver.dto.*;
import com.cts.driver.dto.client.BookingAssignmentDto;
import com.cts.driver.dto.client.UserDto;
import com.cts.driver.exception.OfferException;
import com.cts.driver.exception.ResourceNotFoundException;
import com.cts.driver.mapper.DriverMapper;
import com.cts.driver.mapper.RideOfferMapper;
import com.cts.driver.model.Driver;
import com.cts.driver.model.RideOffer;
import com.cts.driver.model.RideOfferStatus;
import com.cts.driver.repository.DriverRepository;
import com.cts.driver.repository.RideOfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DriverServiceImpl {

    @Autowired private DriverRepository driverRepository;
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
        driver.setPanNumber(dto.getPanNumber());
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setDriverLicenseExpiry(dto.getDriverLicenseExpiry());
        
        // Reset verification status, pending admin approval
        driver.setAadhaarVerified(false);
        driver.setPanVerified(false);
        driver.setDriverLicenseVerified(false);
        
        Driver updatedDriver = driverRepository.save(driver);
        UserDto user = userClient.getUserById(userId);
        return driverMapper.toDriverProfileDto(updatedDriver, user);
    }

    @Transactional
    public boolean updateDriverStatus(String userId, UpdateDriverStatusDto dto) {
        log.info("Updating status for user ID: {} to available={}", userId, dto.isAvailable());
        Driver driver = findDriverByUserId(userId);
        
        if (dto.isAvailable() && !driver.isDriverLicenseVerified()) {
            log.warn("Driver {} cannot go online. Driver is not verified.", userId);
            throw new IllegalStateException("Cannot go online. Your profile is not yet verified by an admin.");
        }
        
        driver.setAvailable(dto.isAvailable());
        driver.setCurrentCity(dto.getCurrentCity());
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
    public void acceptOffer(String userId, String offerId) {
        log.info("Driver (User ID: {}) attempting to accept offer ID: {}", userId, offerId);
        Driver driver = findDriverByUserId(userId);
        
        RideOffer offer = rideOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found. It may have expired."));

        if (!offer.getDriverId().equals(driver.getId())) {
            log.warn("Security violation: Driver {} tried to accept offer {} not belonging to them.", userId, offerId);
            throw new OfferException("This offer does not belong to you.");
        }
        
        if (offer.getStatus() != RideOfferStatus.PENDING) {
            log.warn("Driver {} tried to accept an offer that is already {}.", userId, offer.getStatus());
            throw new OfferException("This offer is no longer available.");
        }
        
        // 1. Claim the offer
        offer.setStatus(RideOfferStatus.ACCEPTED);
        rideOfferRepository.save(offer);

        // 2. Reject all other pending offers for this *booking*
        // List<RideOffer> otherOffers = rideOfferRepository.findByBookingIdAndStatus(offer.getBookingId(), RideOfferStatus.PENDING);
        // for (RideOffer other : otherOffers) {
        //     other.setStatus(RideOfferStatus.REJECTED);
        // }
        // rideOfferRepository.saveAll(otherOffers);

        // 3. Set this driver to UNAVAILABLE (now on a ride)
        driver.setAvailable(false);
        driverRepository.save(driver);

        // 4. Call booking-service via Feign to assign the ride
        try {
            log.info("Calling booking-service to assign booking {} to driver {}", offer.getBookingId(), driver.getUserId());
            bookingClient.assignBooking(offer.getBookingId(), new BookingAssignmentDto(driver.getUserId()));
        } catch (Exception e) {
            log.error("Failed to assign booking {} to driver {}. Rolling back.", offer.getBookingId(), driver.getUserId(), e);
            // This will roll back the transaction (offer status, driver availability)
            throw new RuntimeException("Failed to confirm booking with booking-service. Please try again.", e);
        }
    }
    
    // --- Admin Methods ---
    
    public Page<DriverProfileDto> getAllDrivers(Pageable pageable) {
        log.info("Admin request: Fetching all drivers, page {} size {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Driver> driverPage = driverRepository.findAll(pageable);
        
        return driverPage.map(driver -> {
            UserDto user = userClient.getUserById(driver.getUserId());
            return driverMapper.toDriverProfileDto(driver, user);
        });
    }

    public Page<Driver> getVerificationQueue(Pageable pageable) {
        log.info("Admin request: Fetching verification queue");
        return driverRepository.findByIsDriverLicenseVerifiedFalse(pageable);
    }
    
    @Transactional
    public void approveDriver(String driverId) {
        log.info("Admin request: Approving driver ID: {}", driverId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found."));
        
        driver.setAadhaarVerified(true);
        driver.setPanVerified(true);
        driver.setDriverLicenseVerified(true);
        driverRepository.save(driver);
    }

    // Helper method
    public Driver findDriverByUserId(String userId) {
        return driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId));
    }
}