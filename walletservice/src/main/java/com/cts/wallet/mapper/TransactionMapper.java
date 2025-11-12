package com.cts.wallet.mapper;

import com.cts.wallet.dto.TransactionDto;
import com.cts.wallet.model.WalletTransaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    /**
     * Converts a WalletTransaction (Entity) to a TransactionDto (API Response).
     */
    public TransactionDto toTransactionDto(WalletTransaction tx) {
        return TransactionDto.builder()
                .id(tx.getId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .relatedBookingId(tx.getRelatedBookingId())
                .timestamp(tx.getTimestamp())
                .build();
    }
}