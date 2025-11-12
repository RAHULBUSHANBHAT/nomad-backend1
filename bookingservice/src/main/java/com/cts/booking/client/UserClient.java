package com.cts.booking.client;

import com.cts.booking.config.FeignClientConfig;
import com.cts.booking.dto.AddRatingRequestDto;
import com.cts.booking.dto.client.UserDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client to call the INTERNAL endpoints of user-service.
 * This is for the NEW "Feedback" feature.
 */
@FeignClient(name = "USER-SERVICE", 
             path = "/api/v1/internal/users", 
             configuration = FeignClientConfig.class)
public interface UserClient {

    @PostMapping("/{id}/add-rating")
    ResponseEntity<Void> addRating(
            @PathVariable("id") String id, 
            @RequestBody AddRatingRequestDto dto);

            @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") String id);
}