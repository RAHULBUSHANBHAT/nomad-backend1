package com.cts.wallet.controller;

import com.cts.wallet.dto.DepositRequestDto; // <-- ADD
import jakarta.validation.Valid; // <-- ADD

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // <-- ADD
import org.springframework.web.bind.annotation.RequestBody; // <-- ADD
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.cts.wallet.dto.WalletTransactionDto;
import com.cts.wallet.dto.TransactionFilterDto;
import com.cts.wallet.dto.WalletDto;
import com.cts.wallet.dto.WithdrawRequestDto;
import com.cts.wallet.model.RideTransaction;
import com.cts.wallet.service.WalletService;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1") // We map at the root to handle both /wallets and /admin
@Slf4j
public class WalletController {

    @Autowired
    private WalletService walletService;
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {

        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                // If the incoming string is "null", "NULL", or empty, 
                // treat it as a proper null object.
                if (text == null || text.isEmpty() || "null".equalsIgnoreCase(text)) {
                    setValue(null);
                } else {
                    setValue(text);
                }
            }
        });
        
        binder.registerCustomEditor(Double.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty() || "null".equalsIgnoreCase(text)) {
                    setValue(null); // Set the property to a real null object
                } else {
                    try {
                        setValue(Double.parseDouble(text));
                    } catch (NumberFormatException e) {
                        setValue(null); // Or throw an exception
                    }
                }
            }
        });

        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty() || "null".equalsIgnoreCase(text)) {
                    setValue(null); // Set the property to a real null object
                } else {
                    try {
                        setValue(LocalDate.parse(text));
                    } catch (Exception e) {
                        setValue(null); // Or throw an exception
                    }
                }
            }
        });
    }
    
    private String getUserId(Authentication authentication) {
        return (String) authentication.getDetails();
    }

    // --- USER-FACING ENDPOINTS ---
    
    @GetMapping("/wallets/me")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can see their wallet
    public ResponseEntity<WalletDto> getMyWallet(Authentication authentication) {
        log.info("Fetching wallet for user: {}", authentication.getName());
        return ResponseEntity.ok(walletService.getWalletByUserId(getUserId(authentication)));
    }

    @GetMapping("/wallets/me/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<WalletTransactionDto>> getMyTransactions(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching transactions for user: {}", authentication.getName());
        return ResponseEntity.ok(walletService.getTransactionsByUserId(getUserId(authentication), pageable));
    }

    @PostMapping("/wallets/me/deposit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WalletDto> depositFunds(
            Authentication authentication, 
            @Valid @RequestBody DepositRequestDto depositRequest) {
        
        String userId = getUserId(authentication);
        log.info("Received deposit request of {} for user {}", depositRequest.getAmount(), userId);
        WalletDto updatedWallet = walletService.depositFunds(userId, depositRequest.getAmount());
        return ResponseEntity.ok(updatedWallet);
    }

    @PostMapping("/wallets/me/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WalletDto> withdrawFunds(
            Authentication authentication, 
            @Valid @RequestBody WithdrawRequestDto withdrawRequest) {
        
        String userId = getUserId(authentication);
        log.info("Received withdrawal request of {} for user {}", withdrawRequest.getAmount(), userId);
        WalletDto updatedWallet = walletService.withdrawFunds(userId, withdrawRequest.getAmount());
        return ResponseEntity.ok(updatedWallet);
    }

    // --- ADMIN ENDPOINTS (as requested) ---

    @GetMapping("/admin/wallets/wallet-transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<WalletTransactionDto>> getAllWalletTransactions(
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Admin request: Fetching all wallet transactions");
        return ResponseEntity.ok(walletService.getAllWalletTransactions(pageable));
    }

    @GetMapping("/admin/wallets/ride-transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RideTransaction>> getAllRideTransactions(
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            @Valid TransactionFilterDto filters) {
        log.info("Admin request: Fetching all rider transactions");
        return ResponseEntity.ok(walletService.getAllRideTransactions(filters, pageable));
    }
    
    @GetMapping("/admin/wallets/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WalletDto> getWalletByUserIdForAdmin(@PathVariable String userId) {
        log.info("Admin request: Fetching wallet for user ID: {}", userId);
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }
}