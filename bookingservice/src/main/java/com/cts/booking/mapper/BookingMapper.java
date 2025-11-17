package com.cts.booking.mapper;

import com.cts.booking.dto.BookingDto;
import com.cts.booking.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        // Copy basic fields
        dto.setId(booking.getId());
        dto.setStatus(booking.getStatus());
        dto.setRiderUserId(booking.getRiderUserId());
        dto.setRiderName(booking.getRiderName());
        dto.setRiderPhoneNumber(booking.getRiderPhoneNumber());
        dto.setDriverUserId(booking.getDriverUserId());
        dto.setDriverName(booking.getDriverName());
        dto.setDriverPhoneNumber(booking.getDriverPhoneNumber());
        dto.setDriverRating(booking.getDriverProfileRating());
        dto.setTotalTrips(booking.getTotalTrips());
        dto.setDriverCreatedAt(booking.getDriverCreatedAt());
        dto.setVehicleId(booking.getVehicleId()); // Important
        dto.setFare(booking.getFare());
        dto.setPaymentStatus(booking.getPaymentStatus());
        dto.setPickupLocationName(booking.getPickupLocationName());
        dto.setDropoffLocationName(booking.getDropoffLocationName());
        dto.setPickupLat(booking.getPickupLat());
        dto.setPickupLng(booking.getPickupLng());
        dto.setDropoffLat(booking.getDropoffLat());
        dto.setDropoffLng(booking.getDropoffLng());
        dto.setCity(booking.getCity());
        dto.setVehicleType(booking.getVehicleType());
        dto.setRequestTime(booking.getRequestTime());
        dto.setAcceptedTime(booking.getAcceptedTime());
        dto.setPickupTime(booking.getPickupTime());
        dto.setCompletedTime(booking.getCompletedTime());
        return dto;
    }
}