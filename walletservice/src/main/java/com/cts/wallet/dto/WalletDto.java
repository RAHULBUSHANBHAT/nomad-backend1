package com.cts.wallet.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class WalletDto {
    private String id;
    private String userId;
    private double balance;
    private String status;
    private LocalDateTime updatedAt;
}