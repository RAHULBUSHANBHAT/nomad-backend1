package com.cts.wallet.dto;

import java.time.LocalDateTime;
import com.cts.wallet.model.TransactionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletTransactionDto {
    private String id;
    private double amount;
    private TransactionType type;
    private String walletId;
    private String referenceId;
    private LocalDateTime timestamp;
}