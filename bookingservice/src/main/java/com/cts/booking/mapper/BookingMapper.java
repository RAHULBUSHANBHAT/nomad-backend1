package com.cts.booking.mapper;

import com.cts.booking.dto.BookingDto;
import com.cts.booking.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .riderUserId(booking.getRiderUserId())
                .driverUserId(booking.getDriverUserId())
                .status(booking.getStatus())
                .pickupLocationName(booking.getPickupLocationName())
                .dropoffLocationName(booking.getDropoffLocationName())
                .fare(booking.getFare())
                .paymentStatus(booking.getPaymentStatus())
                .requestTime(booking.getRequestTime())
                .completedTime(booking.getCompletedTime())
                .driverRating(booking.getDriverRating())
                .build();
    }
}