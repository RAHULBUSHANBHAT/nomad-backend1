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

    @Column(name = "reference_id", nullable = false)
    private String referenceId; 

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public WalletTransaction(String walletId, double amount, TransactionType type, String referenceId) {
        this.walletId = walletId;
        this.amount = amount;
        this.type = type;
        this.referenceId = (referenceId != null) ? referenceId : UUID.randomUUID().toString();
    }

    @PrePersist
    protected void onPersist() {
        timestamp = LocalDateTime.now();
        if (referenceId == null) {
            referenceId = UUID.randomUUID().toString();
        }
    }
}