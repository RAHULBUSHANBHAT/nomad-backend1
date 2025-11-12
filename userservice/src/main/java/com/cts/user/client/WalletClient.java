package com.cts.user.client;

import com.cts.user.config.FeignClientConfig;
import com.cts.user.dto.TransactionDto;
import com.cts.user.dto.WalletDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "WALLET-SERVICE", 
             path = "/api/v1/internal/wallets", 
             configuration = FeignClientConfig.class)
public interface WalletClient {

    @GetMapping("/user/{userId}")
    WalletDto getWalletByUserIdInternal(@PathVariable("userId") String userId);

    @GetMapping("/transactions/latest")
    List<TransactionDto> getLatestTransactions();
}