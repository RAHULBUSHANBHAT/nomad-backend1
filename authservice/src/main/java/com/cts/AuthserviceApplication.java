package com.cts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
@EnableDiscoveryClient
public class AuthserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthserviceApplication.class, args);
	}

}
