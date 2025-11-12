package com.cts.wallet.dto.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

// This DTO is for the *internal* payment endpoint
@Data
public class RidePaymentRequestDto {
    @NotBlank
    private String bookingId;
    @NotBlank
    private String riderUserId;
    @NotBlank
    private String driverUserId;
    @Positive
    private double fare;
    @Positive
    private double commission;
}