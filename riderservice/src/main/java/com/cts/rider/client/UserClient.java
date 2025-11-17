package com.cts.rider.client;

import com.cts.rider.config.FeignClientConfig;
import com.cts.rider.dto.client.UserDto;
import com.cts.rider.dto.UpdateRiderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "USER-SERVICE", 
             path = "/api/v1/internal/users", 
             configuration = FeignClientConfig.class)
public interface UserClient {

    @GetMapping("/{id}")
    UserDto getUserByIdInternal(@PathVariable("id") String id); 
    
    @PutMapping("/{id}")
    UserDto updateUserInternal(@PathVariable("id") String id, @RequestBody UpdateRiderDto updateUserDto);
}