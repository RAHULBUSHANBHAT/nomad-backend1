package com.cts.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class DepositRequestDto {
    
    @DecimalMin(value = "1.00", message = "Deposit amount must be at least 1.00")
    private double amount;

    // In a real app, this would be "payment_token" from Stripe
}