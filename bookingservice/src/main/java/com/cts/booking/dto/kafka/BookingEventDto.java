package com.cts.booking.dto.kafka;

import com.cts.booking.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is an "FYI" event for other services (analytics, etc.)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingEventDto {
    private String bookingId;
    private String status;
    private String driverId;
    private String riderId;
    private double fare;
    
    public static BookingEventDto fromBooking(Booking booking) {
        return new BookingEventDto(
            booking.getId(), 
            booking.getStatus().name(), 
            booking.getDriverUserId(), 
            booking.getRiderUserId(),
            booking.getFare()
        );
    }
}