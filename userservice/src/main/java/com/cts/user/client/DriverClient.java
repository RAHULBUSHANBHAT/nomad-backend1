package com.cts.user.client;

import com.cts.user.config.FeignClientConfig;
import com.cts.user.dto.UpdateDriverStatusDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "DRIVER-SERVICE", 
             path = "/api/v1/internal/drivers", 
             configuration = FeignClientConfig.class)
public interface DriverClient {

    @GetMapping("/count")
    long getDriverCount();

    @PutMapping("/me/status/{userId}")
    void updateMyLocation(@PathVariable("userId") String userId, UpdateDriverStatusDto statusDto);
}