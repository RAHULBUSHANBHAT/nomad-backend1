package com.cts.wallet.model;

public enum TransactionType {
    DEPOSIT,        // Money added by user
    WITHDRAWAL,     // Money taken out by user
    RIDE_DEBIT,     // Payment for a ride (Rider)
    RIDE_CREDIT,    // Payout for a ride (Driver)
    COMMISSION_FEE  // Company commission
}