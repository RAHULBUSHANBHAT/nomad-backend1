package com.cts.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
@EnableDiscoveryClient
public class UserserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserserviceApplication.class, args);
	}

}
