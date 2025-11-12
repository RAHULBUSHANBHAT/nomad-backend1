package com.cts.wallet.mapper;

import com.cts.wallet.dto.WalletTransactionDto;
import com.cts.wallet.dto.WalletDto;
import com.cts.wallet.model.RideTransaction;
import com.cts.wallet.model.Wallet;
import com.cts.wallet.model.WalletTransaction;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletDto toWalletDto(Wallet wallet) {
        return WalletDto.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .status(wallet.getStatus())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    public WalletTransactionDto toWalletTransactionDto(WalletTransaction tx) {
        return WalletTransactionDto.builder()
                .id(tx.getId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .userId(tx.getUserId())
                .timestamp(tx.getTimestamp())
                .build();
    }
}