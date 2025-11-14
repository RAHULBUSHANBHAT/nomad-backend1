package com.cts.user.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AdminDashboardDto {
    private double companyWalletBalance;
    private long totalRiders;
    private long totalDrivers;
    private long totalVehicles;
    private long successfulTrips;
    private List<?> latestTransactions;
}