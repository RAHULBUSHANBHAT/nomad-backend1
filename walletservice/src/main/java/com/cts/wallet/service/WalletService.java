package com.cts.wallet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.wallet.model.PaymentMode;
import com.cts.wallet.model.RideTransaction;
import com.cts.wallet.model.TransactionType; // <-- ADD

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.cts.wallet.dto.WalletTransactionDto;
import com.cts.wallet.dto.TransactionFilterDto;
import com.cts.wallet.dto.WalletDto;
import com.cts.wallet.dto.internal.RidePaymentRequestDto;
import com.cts.wallet.exception.InsufficientFundsException;
import com.cts.wallet.exception.ResourceNotFoundException;
import com.cts.wallet.mapper.WalletMapper;
import com.cts.wallet.model.Wallet;
import com.cts.wallet.model.WalletTransaction;
import com.cts.wallet.repository.RideTransactionRepository;
import com.cts.wallet.repository.TransactionSpecification;
import com.cts.wallet.repository.WalletRepository;
import com.cts.wallet.repository.WalletTransactionRepository;


@Service

public class WalletService {
    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    @Autowired private WalletMapper walletMapper;
    @Autowired private WalletRepository walletRepository;
    @Autowired private TransactionSpecification specBuilder;
    @Autowired private WalletTransactionRepository walletTransactionRepository;
    @Autowired private RideTransactionRepository rideTransactionRepository;

    @Value("${app.company-wallet-user-id}")
    private String companyWalletUserId;

    /**
     * Called by Kafka consumer to create a wallet for a new user.
     */
    @Transactional
    public void createWallet(String userId) {
        if (walletRepository.findByUserId(userId).isPresent()) {
            log.warn("Wallet already exists for user ID: {}", userId);
            return;
        }
        Wallet wallet = new Wallet(userId);
        
        // Create the company wallet on first-ever run
        if (!walletRepository.findByUserId(companyWalletUserId).isPresent()) {
            log.info("Company wallet not found. Creating special company wallet now.");
            walletRepository.save(new Wallet(companyWalletUserId));
        }

        walletRepository.save(wallet);
        log.info("Created new wallet for user ID: {}", userId);
    }
    
    @Transactional(readOnly = true)
    public WalletDto getWalletByUserId(String userId) {
        Wallet wallet = findWalletByUserId(userId);
        return walletMapper.toWalletDto(wallet);
    }

    @Transactional(readOnly = true)
    public Page<WalletTransactionDto> getTransactionsByUserId(String userId, Pageable pageable) {
        Wallet wallet = findWalletByUserId(userId);
        return walletTransactionRepository.findByWalletId(wallet.getId(), pageable)
                .map(walletMapper::toWalletTransactionDto);
    }

    @Transactional(readOnly = true)
    public Page<WalletTransactionDto> getAllWalletTransactions(TransactionFilterDto filters, Pageable pageable) {
        return walletTransactionRepository.findAll(pageable).map(walletMapper::toWalletTransactionDto);
    }

    @Transactional(readOnly = true)
    public Page<RideTransaction> getAllRideTransactions(TransactionFilterDto filters, Pageable pageable) {
        Specification<RideTransaction> spec = Specification
            .where(specBuilder.hasBookingId(filters.getSearchTerm()))
            .and(specBuilder.hasType(filters.getType()))
            .and(specBuilder.hasFare(filters.getFareFilter(), filters.getFareValue()))
            .and(specBuilder.hasDate(filters.getDateFilter()));
        return rideTransactionRepository.findAll(spec, pageable);
    }

    /**
     * This is the core transactional logic for a ride payment.
     * It is ATOMIC. It all succeeds, or it all fails.
     */
    @Transactional
    public void executeRidePayment(RidePaymentRequestDto request) {
        log.info("Executing payment for booking ID: {}", request.getBookingId());
        
        // 1. Find all wallets
        Wallet riderWallet = findWalletByUserId(request.getRiderUserId());
        Wallet driverWallet = findWalletByUserId(request.getDriverUserId());
        Wallet companyWallet = findWalletByUserId(companyWalletUserId); 

        // 2. Check for sufficient funds
        if (riderWallet.getBalance() < request.getTotalFare()) {
            log.warn("Payment failed: Rider {} has insufficient funds.", request.getRiderUserId());
            throw new InsufficientFundsException("Insufficient funds to pay for ride.");
        }
        
        double driverPayout = request.getTotalFare() - request.getCommissionFee();

        if(request.getPaymentMode().equals(PaymentMode.CASH)) {
            driverWallet.setBalance(driverWallet.getBalance() - driverPayout);
            walletTransactionRepository.save(new WalletTransaction(
                driverWallet.getId(), -driverPayout, TransactionType.RIDE_CREDIT, request.getDriverUserId(), request.getBookingId()));
        } else {
            riderWallet.setBalance(riderWallet.getBalance() - request.getTotalFare());
            walletTransactionRepository.save(new WalletTransaction(
                riderWallet.getId(), -request.getTotalFare(), TransactionType.RIDE_DEBIT, request.getRiderUserId(), request.getBookingId()));

            driverWallet.setBalance(driverWallet.getBalance() + driverPayout);
            walletTransactionRepository.save(new WalletTransaction(
                driverWallet.getId(), driverPayout, TransactionType.RIDE_CREDIT, request.getDriverUserId(), request.getBookingId()));
        }

        // 5. Credit Company
        companyWallet.setBalance(companyWallet.getBalance() + request.getCommissionFee());
        walletTransactionRepository.save(new WalletTransaction(
            companyWallet.getId(), request.getCommissionFee(), TransactionType.COMMISSION_FEE, companyWalletUserId, request.getBookingId()));

        rideTransactionRepository.save(new RideTransaction(
            request.getBookingId(), request.getRiderUserId(), request.getRiderName(),
            request.getRiderPhone(), request.getDriverUserId(), request.getDriverName(),
            request.getDriverPhone(), request.getBaseFare(), request.getDistanceFare(), request.getTaxes(),
            request.getCommissionFee(), request.getTotalFare(), request.getPickupAddress(),
            request.getDropoffAddress(), request.getPaymentMode()));
            
        // 6. Save all wallet changes (JPA will batch these)
        walletRepository.save(riderWallet);
        walletRepository.save(driverWallet);
        walletRepository.save(companyWallet);
        
        log.info("Payment successful for booking ID: {}", request.getBookingId());
    }
    @Transactional
    public WalletDto depositFunds(String userId, double amount) {
        log.info("Attempting to deposit {} for user ID: {}", amount, userId);
        
        Wallet wallet = findWalletByUserId(userId);

        wallet.setBalance(wallet.getBalance() + amount);
        Wallet savedWallet = walletRepository.save(wallet);

        // --- THIS WAS THE MISSING LINE ---
        walletTransactionRepository.save(new WalletTransaction(
            wallet.getId(), 
            amount, // Positive amount
            TransactionType.DEPOSIT,
            userId,
            null // No booking ID for a deposit
        ));
        log.info("Deposit successful. New balance for user {}: {}", userId, savedWallet.getBalance());
        return walletMapper.toWalletDto(savedWallet);
    }

    @Transactional
    public WalletDto withdrawFunds(String userId, double amount) {
        log.info("Attempting to withdraw {} for user ID: {}", amount, userId);
        
        Wallet wallet = findWalletByUserId(userId);

        // --- Core Logic ---
        if (wallet.getBalance() < amount) {
            log.warn("Withdrawal failed: User {} has insufficient funds. Balance: {}, Requested: {}", 
                     userId, wallet.getBalance(), amount);
            throw new InsufficientFundsException("Insufficient funds for withdrawal.");
        }
        // --------------------

        wallet.setBalance(wallet.getBalance() - amount);
        Wallet savedWallet = walletRepository.save(wallet);

        // --- Create Transaction Record ---
        walletTransactionRepository.save(new WalletTransaction(
            wallet.getId(), 
            -amount, // Store as a negative amount
            TransactionType.WITHDRAWAL, 
            userId,
            null // No booking ID for a withdrawal
        ));
        
        log.info("Withdrawal successful. New balance for user {}: {}", userId, savedWallet.getBalance());
        return walletMapper.toWalletDto(savedWallet);
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionDto> getLatest5Transactions() {
        log.info("Fetching latest 5 transactions across all wallets.");
        List<WalletTransaction> transactions = walletTransactionRepository.findAll(
                Sort.by(Sort.Direction.DESC, "timestamp")).stream()
                .limit(5)
                .toList();
        return transactions.stream()
                .map(walletMapper::toWalletTransactionDto)
                .toList();
    }
    // Helper method
    private Wallet findWalletByUserId(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user ID: " + userId));
    }
}