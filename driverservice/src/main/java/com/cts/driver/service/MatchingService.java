// package com.cts.driver.service;

// import com.cts.driver.dto.kafka.RideRequestEventDto;
// import com.cts.driver.model.Driver;
// import com.cts.driver.model.RideOffer;
// import com.cts.driver.model.VehicleType;
// import com.cts.driver.repository.DriverRepository;
// import com.cts.driver.repository.RideOfferRepository;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.List;

// @Service
// @Slf4j
// public class MatchingService {

//     @Autowired private DriverRepository driverRepository;
//     @Autowired private RideOfferRepository rideOfferRepository;

//     /**
//      * This is our "Simple Matcher".
//      */
//     @Transactional
//     public void findDriversAndCreateOffers(RideRequestEventDto eventDto) {
//         String city = eventDto.getCity();
//         VehicleType type = VehicleType.valueOf(eventDto.getVehicleType().toUpperCase());
        
//         log.info("Matcher: Finding drivers in city {} for type {}", city, type);

//         // 1. Find all matching drivers (our simple SQL query)
//         List<Driver> availableDrivers = driverRepository.findAvailableDrivers(city, type);

//         if (availableDrivers.isEmpty()) {
//             log.warn("No drivers found for booking {}. Booking will time out.", eventDto.getBookingId());
//             // We could send a "NO_DRIVERS_FOUND" event back to booking-service
//             return;
//         }

//         // 2. Create an offer for each driver
//         for (Driver driver : availableDrivers) {
//             RideOffer offer = new RideOffer(eventDto.getBookingId(), driver.getId());
//             rideOfferRepository.save(offer);
//         }
        
//         log.info("Created {} offers for booking {}", availableDrivers.size(), eventDto.getBookingId());
//     }
// }