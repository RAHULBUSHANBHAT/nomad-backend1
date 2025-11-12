// package com.cts.rider.client;

// import com.cts.rider.config.FeignClientConfig; // Import
// import com.cts.rider.dto.client.BookingDto;
// import org.springframework.cloud.openfeign.FeignClient;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable; // Import

// /**
//  * Feign Client to call the INTERNAL endpoints of booking-service.
//  */
// @FeignClient(name = "BOOKING-SERVICE", 
//              path = "/api/v1/internal/bookings", 
//              configuration = FeignClientConfig.class) // <-- This is the fix
// public interface BookingClient {

//     // This is the internal endpoint for the Rider's "My Bookings" page
//     @GetMapping("/rider/{riderId}")
//     Page<BookingDto> getMyBookingHistoryInternal(@PathVariable("riderId") String riderId, Pageable pageable);
// }