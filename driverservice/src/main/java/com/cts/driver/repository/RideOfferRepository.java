package com.cts.driver.repository;

import com.cts.driver.model.RideOffer;
import com.cts.driver.model.RideOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideOfferRepository extends JpaRepository<RideOffer, String> {
    
    // For the driver to poll for new offers
    List<RideOffer> findByDriverIdAndStatus(String driverId, RideOfferStatus status);
    
    // To clear out other offers when one is accepted
    // List<RideOffer> findByBookingIdAndStatus(String bookingId, RideOfferStatus status);
}