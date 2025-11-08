package com.cts.driver.mapper;

import com.cts.driver.dto.RideOfferDto;
import com.cts.driver.model.RideOffer;
import org.springframework.stereotype.Component;

@Component
public class RideOfferMapper {
    
    public RideOfferDto toDto(RideOffer offer) {
        return RideOfferDto.builder()
                .id(offer.getId())
                .bookingId(offer.getBookingId())
                .status(offer.getStatus())
                .createdAt(offer.getCreatedAt())
                .build();
    }
}