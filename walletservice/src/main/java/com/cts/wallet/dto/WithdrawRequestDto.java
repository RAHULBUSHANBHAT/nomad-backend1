package com.cts.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class WithdrawRequestDto {
    
    @DecimalMin(value = "1.00", message = "Withdrawal amount must be at least 1.00")
    private double amount;
}