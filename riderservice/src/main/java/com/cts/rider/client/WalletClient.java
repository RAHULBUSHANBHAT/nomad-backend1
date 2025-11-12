package com.cts.rider.client;

import com.cts.rider.config.FeignClientConfig; // <-- Import
import com.cts.rider.dto.client.WalletDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // <-- Import

/**
 * Feign Client to call the INTERNAL endpoints of wallet-service.
 */
@FeignClient(name = "WALLET-SERVICE", 
             path = "/api/v1/internal/wallets", 
             configuration = FeignClientConfig.class) // <-- This is the fix
public interface WalletClient {

    @GetMapping("/user/{userId}") // This matches WalletInternalController
    WalletDto getWalletByUserIdInternal(@PathVariable("userId") String userId);
}