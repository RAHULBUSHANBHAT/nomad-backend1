package com.cts.wallet.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "wallet_transactions")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "wallet_id", nullable = false)
    private String walletId;

    @Column(name = "amount", nullable = false)
    private double amount; 

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    // Renamed from 'relatedBookingId' to be generic
    @Column(name = "reference_id", nullable = false)
    private String referenceId; 

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // --- THE CRITICAL CONSTRUCTOR ---
    // This ensures we can create a transaction easily from the Service
    public WalletTransaction(String walletId, double amount, TransactionType type, String referenceId) {
        this.walletId = walletId;
        this.amount = amount;
        this.type = type;
        // If no reference provided (e.g. deposit), generate one. 
        // If booking ID provided, use it.
        this.referenceId = (referenceId != null) ? referenceId : UUID.randomUUID().toString();
    }

    @PrePersist
    protected void onPersist() {
        timestamp = LocalDateTime.now();
        // Double check to ensure non-null before saving
        if (referenceId == null) {
            referenceId = UUID.randomUUID().toString();
        }
    }
}