package com.cts.booking.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

// DTO for calling the wallet-service
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