package com.cts.booking.client;

import com.cts.booking.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client to call the INTERNAL endpoints of driver-service.
 */
@FeignClient(name = "DRIVER-SERVICE", 
             path = "/api/v1/internal/drivers", 
             configuration = FeignClientConfig.class)
public interface DriverClient {

    @PostMapping("/{userId}/availability")
    ResponseEntity<Boolean> setAvailability(@PathVariable("userId") String userId, @RequestParam("available") boolean available);
}