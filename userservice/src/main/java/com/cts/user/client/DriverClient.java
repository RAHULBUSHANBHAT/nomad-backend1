package com.cts.user.client;

import com.cts.user.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "DRIVER-SERVICE", 
             path = "/api/v1/internal/drivers", 
             configuration = FeignClientConfig.class)
public interface DriverClient {

    @GetMapping("/count")
    long getDriverCount();
}