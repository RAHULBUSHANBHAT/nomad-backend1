package com.cts.wallet.dto.internal;

import com.cts.wallet.model.PaymentMode;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// This DTO is for the *internal* payment endpoint
@Data
public class RidePaymentRequestDto {
    @NotBlank
    private String bookingId;
    @NotBlank
    private String riderUserId;
    @NotBlank
    private String riderName;
    @NotBlank
    private String riderPhone;
    @NotBlank
    private String driverUserId;
    @NotBlank
    private String driverName;
    @NotBlank
    private String driverPhone;
    @NotBlank
    private double baseFare;
    @NotBlank
    private double distanceFare;
    @NotBlank
    private double taxes;
    @NotBlank
    private double commissionFee;
    @NotBlank
    private double totalFare;
    @NotBlank
    private String pickupAddress;
    @NotBlank
    private String dropoffAddress;
    @NotBlank
    private PaymentMode paymentMode;
}