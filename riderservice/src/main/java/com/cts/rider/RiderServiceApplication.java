package com.cts.rider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// This is the fix for the "default password" error
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class }) // <-- THE FIX
@EnableDiscoveryClient
@EnableFeignClients
public class RiderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RiderServiceApplication.class, args);
	}

}