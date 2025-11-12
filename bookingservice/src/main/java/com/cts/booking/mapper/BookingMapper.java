package com.cts.booking.mapper;

import com.cts.booking.client.UserClient;
import com.cts.booking.dto.BookingDto;
import com.cts.booking.dto.client.UserDto;
import com.cts.booking.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    @Autowired private UserClient userClient;

    public BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        // Copy basic fields
        dto.setId(booking.getId());
        dto.setStatus(booking.getStatus());
        dto.setRiderUserId(booking.getRiderUserId());
        dto.setDriverUserId(booking.getDriverUserId());
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

        // --- FETCH RIDER INFO ---
        if (booking.getRiderUserId() != null) {
            try {
                UserDto rider = userClient.getUserById(booking.getRiderUserId());
                dto.setRiderName(rider.getFirstName() + " " + rider.getLastName());
                dto.setRiderPhoneNumber(rider.getPhoneNumber());
            } catch (Exception e) { dto.setRiderName("Unknown"); }
        }

        // --- FETCH DRIVER INFO ---
        if (booking.getDriverUserId() != null) {
            try {
                UserDto driver = userClient.getUserById(booking.getDriverUserId());
                dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());
                dto.setDriverPhoneNumber(driver.getPhoneNumber());
                dto.setDriverRating((double)driver.getRating());
            } catch (Exception e) { dto.setDriverName("Unknown"); }
        }
        return dto;
    }
}