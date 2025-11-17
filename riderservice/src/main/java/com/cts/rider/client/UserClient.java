package com.cts.rider.client;

import com.cts.rider.config.FeignClientConfig; // Import
import com.cts.rider.dto.client.UserDto;
import com.cts.rider.dto.UpdateRiderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping; // Import
import org.springframework.web.bind.annotation.RequestBody; // Import


@FeignClient(name = "USER-SERVICE", 
             path = "/api/v1/internal/users", 
             configuration = FeignClientConfig.class) // <-- This is the fix
public interface UserClient {

    @GetMapping("/{id}") // This matches UserInternalController
    UserDto getUserByIdInternal(@PathVariable("id") String id); 
    
    // This now matches the new internal endpoint we will add to user-service
    @PutMapping("/{id}")
    UserDto updateUserInternal(@PathVariable("id") String id, @RequestBody UpdateRiderDto updateUserDto);
}