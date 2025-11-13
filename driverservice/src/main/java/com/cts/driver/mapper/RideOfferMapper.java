package com.cts.driver.mapper;

import com.cts.driver.dto.RideOfferDto;
import com.cts.driver.model.RideOffer;
import org.springframework.stereotype.Component;

@Component
public class RideOfferMapper {
    
    public RideOfferDto toDto(RideOffer offer) {
        return RideOfferDto.builder()
                .offerId(offer.getId())
                .bookingId(offer.getBookingId())
                .fare(offer.getFare())
                .vehicleCategory(offer.getVehicleCategory())
                .expiresAt(offer.getExpiresAt())
                .pickupLocationName(offer.getPickupLocationName())
                .dropoffLocationName(offer.getDropoffLocationName())
                .estimatedDistanceKm(offer.getDistanceInKm())
                .createdAt(offer.getCreatedAt())
                .build();
    }
}