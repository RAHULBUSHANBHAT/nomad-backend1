package com.cts.wallet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.wallet.dto.RideTransactionInternalDto;
import com.cts.wallet.dto.TransactionFilterDto;
import com.cts.wallet.dto.WalletDto;
import com.cts.wallet.dto.WalletTransactionDto;
import com.cts.wallet.dto.internal.RidePaymentRequestDto;
import com.cts.wallet.exception.InsufficientFundsException;
import com.cts.wallet.exception.ResourceNotFoundException;
import com.cts.wallet.mapper.WalletMapper;
import com.cts.wallet.model.PaymentMode;
import com.cts.wallet.model.RideTransaction;
import com.cts.wallet.model.TransactionType;
import com.cts.wallet.model.Wallet;
import com.cts.wallet.model.WalletTransaction;
import com.cts.wallet.repository.RideTransactionRepository;
import com.cts.wallet.repository.TransactionSpecification;
import com.cts.wallet.repository.WalletRepository;
import com.cts.wallet.repository.WalletTransactionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

    // --- 1. PUBLIC METHODS CALLED BY CONTROLLER ---

    @Transactional
    public void executeRidePayment(RidePaymentRequestDto request) {
        // This simply delegates to the core logic method
        processRidePayment(request);
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
    public Page<WalletTransactionDto> getAllWalletTransactions(Pageable pageable) {
        return walletTransactionRepository.findAll(pageable).map(walletMapper::toWalletTransactionDto);
    }

    @Transactional(readOnly = true)
    public Page<RideTransaction> getAllRideTransactions(TransactionFilterDto filters, Pageable pageable) {
        if(filters == null) return rideTransactionRepository.findAll(pageable);
        
        Specification<RideTransaction> spec = Specification
                .where(specBuilder.hasBookingId(filters.getSearchTerm()))
                .and(specBuilder.hasPaymentMode(filters.getPaymentMode()))
                .and(specBuilder.hasFare(filters.getFareFilter(), filters.getFareValue()))
                .and(specBuilder.hasDate(filters.getDateFilter()));
                
        return rideTransactionRepository.findAll(spec, pageable);
    }

    // --- 2. CORE LOGIC (PAYMENTS) ---

    @Transactional
    public void processRidePayment(RidePaymentRequestDto dto) {
        Wallet riderWallet = getWallet(dto.getRiderUserId());
        Wallet driverWallet = getWallet(dto.getDriverUserId());
        Wallet companyWallet = getWallet(companyWalletUserId);

        // Use DTO totalFare (which includes everything) or calculate
        double total = dto.getTotalFare(); 
        double commission = dto.getCommissionFee();
        double driverShare = total - commission;

        if (riderWallet.getBalance() < total) throw new InsufficientFundsException("Rider funds insufficient.");

        // Transfers
        riderWallet.setBalance(riderWallet.getBalance() - total);
        driverWallet.setBalance(driverWallet.getBalance() + driverShare);
        companyWallet.setBalance(companyWallet.getBalance() + commission);
        
        walletRepository.save(riderWallet);
        walletRepository.save(driverWallet);
        walletRepository.save(companyWallet);

        // Ledger
        logTransaction(riderWallet.getId(), -total, TransactionType.RIDE_DEBIT, dto.getBookingId());
        logTransaction(driverWallet.getId(), driverShare, TransactionType.RIDE_CREDIT, dto.getBookingId());
        logTransaction(companyWallet.getId(), commission, TransactionType.COMMISSION_FEE, dto.getBookingId());
        
        // Receipt
        createReceipt(dto, total, commission, "WALLET");
    }

    @Transactional
    public void processCashPayment(RidePaymentRequestDto dto) {
        Wallet riderWallet = getWallet(dto.getRiderUserId());
        Wallet driverWallet = getWallet(dto.getDriverUserId());
        Wallet companyWallet = getWallet(companyWalletUserId);
        double commission = dto.getCommissionFee();

        // Deduct Commission Only
        driverWallet.setBalance(driverWallet.getBalance() - commission);
        companyWallet.setBalance(companyWallet.getBalance() + commission);
        
        walletRepository.save(driverWallet);
        walletRepository.save(companyWallet);

        logTransaction(riderWallet.getId(), dto.getTotalFare(), TransactionType.RIDE_DEBIT, dto.getBookingId());
        logTransaction(driverWallet.getId(), -commission, TransactionType.COMMISSION_FEE, dto.getBookingId());
        logTransaction(companyWallet.getId(), commission, TransactionType.COMMISSION_FEE, dto.getBookingId());
        
        createReceipt(dto, dto.getTotalFare(), commission, "CASH");
    }

    @Transactional
    public WalletDto depositFunds(String userId, double amount) {
        Wallet wallet = getWallet(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        Wallet saved = walletRepository.save(wallet);
        
        logTransaction(saved.getId(), amount, TransactionType.DEPOSIT, null); // Null ref ID for deposits
        return walletMapper.toWalletDto(saved);
    }

    @Transactional
    public WalletDto withdrawFunds(String userId, double amount) {
        Wallet wallet = getWallet(userId);
        if (wallet.getBalance() < amount) throw new InsufficientFundsException("Insufficient funds.");
        
        wallet.setBalance(wallet.getBalance() - amount);
        Wallet saved = walletRepository.save(wallet);
        
        logTransaction(saved.getId(), -amount, TransactionType.WITHDRAWAL, null);
        return walletMapper.toWalletDto(saved);
    }

    @Transactional(readOnly = true)
    public RideTransactionInternalDto getLatest5Transactions() {
        long successfulTransactions = rideTransactionRepository.count();
        List<RideTransaction> latestTransactions = rideTransactionRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"))
                .stream().limit(5).toList();
        return new RideTransactionInternalDto(successfulTransactions, latestTransactions);
    }

    @Transactional
    public void createWallet(String userId) {
        if (walletRepository.findByUserId(userId).isEmpty()) {
            walletRepository.save(new Wallet(userId));
        }
        if (walletRepository.findByUserId(companyWalletUserId).isEmpty()) {
            walletRepository.save(new Wallet(companyWalletUserId));
        }
    }

    // --- 3. HELPERS ---

    // Helper: Find wallet or throw (for read-only ops)
    private Wallet findWalletByUserId(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user ID: " + userId));
    }

    // Helper: Get existing or create new (for transactional ops)
    private Wallet getWallet(String userId) {
        return getOrCreateWallet(userId);
    }

    

    // Helper: The actual implementation
    private Wallet getOrCreateWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(new Wallet(userId)));
    }

    private void logTransaction(String wId, double amt, TransactionType type, String refId) {
        WalletTransaction txn = new WalletTransaction(wId, amt, type, refId); 
        walletTransactionRepository.save(txn);
    }


    private void createReceipt(RidePaymentRequestDto dto, double total, double comm, String mode) 
    {
        RideTransaction rt = new RideTransaction();
        rt.setBookingId(dto.getBookingId());
        rt.setRiderId(dto.getRiderUserId());
        rt.setRiderName(dto.getRiderName()); // Fill details from DTO
        rt.setRiderPhone(dto.getRiderPhone());

        rt.setPaymentMode(mode == "WALLET" ? PaymentMode.WALLET : PaymentMode.CASH);
        
        rt.setDriverId(dto.getDriverUserId());
        rt.setDriverName(dto.getDriverName());
        rt.setDriverPhone(dto.getDriverPhone());
        
        rt.setPickupAddress(dto.getPickupAddress());
        rt.setDropoffAddress(dto.getDropoffAddress());

        // FIX: Using BigDecimal.valueOf
        rt.setTotalFare((total));
        rt.setCommissionFee((comm));
        rt.setBaseFare((dto.getBaseFare()));
        rt.setDistanceFare((dto.getDistanceFare()));
        rt.setTaxes((dto.getTaxes()));

        rideTransactionRepository.save(rt);
    }
}