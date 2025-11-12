package com.cts.wallet.controller;
import com.cts.wallet.dto.WalletDto; // <-- Import
import com.cts.wallet.dto.TransactionDto; // <-- ADD
import com.cts.wallet.dto.internal.RidePaymentRequestDto;
import com.cts.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// <-- ADD
import org.springframework.web.bind.annotation.*; // <-- Import
import java.util.List;
/**
 * Controller for SECURE, INTERNAL, service-to-service communication.
 * Secured *only* by Layer 1 (GatewayKeyFilter).
 */
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

    /**
     * --- THIS IS THE NEW, MISSING METHOD ---
     * An internal endpoint for other services (like rider-service)
     * to fetch a wallet by its USER ID.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletDto> getWalletByUserIdInternal(@PathVariable String userId) {
        log.info("Internal request: Fetching wallet for user ID: {}", userId);
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @GetMapping("/transactions/latest")
    public ResponseEntity<List<TransactionDto>> getLatestTransactions() {
        log.info("Internal request: getLatestTransactions");
        return ResponseEntity.ok(walletService.getLatest5Transactions());
    }
}