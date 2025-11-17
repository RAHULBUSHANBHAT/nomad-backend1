package com.cts.driver.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cts.driver.dto.client.BookingAssignmentDto;

@FeignClient(name = "BOOKING-SERVICE", path = "/api/v1/internal/bookings")
public interface BookingClient {

    @PutMapping("/{bookingId}/assign")
    ResponseEntity<Void> assignBooking(
            @PathVariable("bookingId") String bookingId, 
            @RequestBody BookingAssignmentDto assignmentDto);
}