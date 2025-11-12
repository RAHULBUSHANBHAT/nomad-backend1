package com.cts.wallet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "wallet_transactions")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "wallet_id", nullable = false)
    private String walletId; // The ID of the wallet this transaction belongs to

    @Column(name = "amount", nullable = false)
    private double amount; // Positive for credit, negative for debit

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "related_booking_id")
    private String relatedBookingId; // Link to the booking (if applicable)

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public WalletTransaction(String walletId, double amount, TransactionType type, String relatedBookingId) {
        this.walletId = walletId;
        this.amount = amount;
        this.type = type;
        this.relatedBookingId = relatedBookingId;
    }

    @PrePersist
    protected void onPersist() {
        timestamp = LocalDateTime.now();
    }
}