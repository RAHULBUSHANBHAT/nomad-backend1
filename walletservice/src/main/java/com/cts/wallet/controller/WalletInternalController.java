package com.cts.wallet.controller;

import com.cts.wallet.dto.RideTransactionInternalDto;
import com.cts.wallet.dto.WalletDto;
import com.cts.wallet.dto.internal.RidePaymentRequestDto;
import com.cts.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/wallets")
@Slf4j
public class WalletInternalController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/execute-payment")
    public ResponseEntity<Void> executeRidePayment(@Valid @RequestBody RidePaymentRequestDto request) {
        log.info("Internal request: Received payment execution for booking ID: {}", request.getBookingId());
        walletService.executeRidePayment(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payment/execute-cash")
    public ResponseEntity<Void> executeCashPayment(@Valid @RequestBody RidePaymentRequestDto paymentDto) {
        log.info("Internal Request: Executing CASH payment for booking {}", paymentDto.getBookingId());
        walletService.processCashPayment(paymentDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletDto> getWalletByUserIdInternal(@PathVariable String userId) {
        log.info("Internal request: Fetching wallet for user ID: {}", userId);
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @GetMapping("/transactions/latest")
    public ResponseEntity<RideTransactionInternalDto> getLatestTransactions() {
        log.info("Internal request: getLatestTransactions");
        return ResponseEntity.ok(walletService.getLatest5Transactions());
    }
}