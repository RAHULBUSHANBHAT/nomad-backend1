package com.cts.driver.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.cts.driver.dto.client.UserDto;


@FeignClient(name = "USER-SERVICE", path = "/api/v1/internal/users")
public interface UserClient {

    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") String id);

   @GetMapping("/search") 
    UserDto searchUser(@RequestParam(value = "email", required = false) String email,
                       @RequestParam(value = "phone", required = false) String phone);
}