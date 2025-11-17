package com.cts.booking.client;

import com.cts.booking.dto.client.RidePaymentRequestDto;
import com.cts.booking.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "WALLET-SERVICE", 
             path = "/api/v1/internal/wallets", 
             configuration = FeignClientConfig.class)
public interface WalletClient {

    @PostMapping("/execute-payment")
    ResponseEntity<Void> executeRidePayment(@RequestBody RidePaymentRequestDto request);
    @PostMapping("/payment/execute-cash")
    ResponseEntity<Void> executeCashPayment(@RequestBody RidePaymentRequestDto request);
}