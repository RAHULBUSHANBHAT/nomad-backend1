package com.cts.wallet.mapper;

import com.cts.wallet.dto.WalletTransactionDto;
import com.cts.wallet.model.WalletTransaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    /**
     * Converts a WalletTransaction (Entity) to a TransactionDto (API Response).
     */
    public WalletTransactionDto toTransactionDto(WalletTransaction tx) {
        return WalletTransactionDto.builder()
                .id(tx.getId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .userId(tx.getUserId())
                .timestamp(tx.getTimestamp())
                .build();
    }
}