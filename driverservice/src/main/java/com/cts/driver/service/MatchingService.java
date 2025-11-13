package com.cts.driver.service;

import com.cts.driver.dto.kafka.RideRequestEventDto;
import com.cts.driver.model.Driver;
import com.cts.driver.model.RideOffer;
import com.cts.driver.repository.DriverRepository;
import com.cts.driver.repository.RideOfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class MatchingService {

    @Autowired private DriverRepository driverRepository;
    @Autowired private RideOfferRepository rideOfferRepository;

    @Transactional
    public void findDriversAndCreateOffers(RideRequestEventDto request) {
        log.info("Finding match for booking {} in {} for category {}", 
                 request.getBookingId(), request.getCity(), request.getVehicleType());

        // 1. Find Available Drivers in the City
        List<Driver> availableDrivers = driverRepository.findByCurrentCityAndAvailableTrue(request.getCity());

        int offerCount = 0;
        for (Driver driver : availableDrivers) {
            
            // 2. Check if Driver has a VERIFIED vehicle matching the category
            // FIX: Convert Enum to String using .name() before comparing
            boolean hasMatchingVehicle = driver.getVehicles().stream()
                .anyMatch(v -> v.isVerified() && 
                               v.getVehicleType().name().equalsIgnoreCase(request.getVehicleType()));

            if (hasMatchingVehicle) {
                RideOffer offer = new RideOffer(
                    request.getBookingId(),
                    driver.getId(),
                    request.getVehicleType(),
                    request.getFare(),
                    request.getPickupLocationName(),
                    request.getDropoffLocationName(),
                    request.getEstimatedDistanceKm()
                );
                rideOfferRepository.save(offer);
                offerCount++;
            }
        }
        log.info("Created {} ride offers for booking {}", offerCount, request.getBookingId());
    }
}