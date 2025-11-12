package com.cts.user.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AdminDashboardDto {
    private long totalRiders;
    private long totalDrivers;
    private double companyWalletBalance;
    private List<TransactionDto> latestTransactions; // We need a TransactionDto
}