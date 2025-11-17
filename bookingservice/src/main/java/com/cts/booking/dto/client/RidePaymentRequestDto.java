package com.cts.booking.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RidePaymentRequestDto {
    private String bookingId;
    
    private String riderUserId;
    private String riderName;
    private String riderPhone;
    
    private String driverUserId;
    private String driverName;
    private String driverPhone;
    
    private double baseFare;
    private double distanceFare;
    private double taxes;
    private double commissionFee;
    private double totalFare;
    
    private String pickupAddress;
    private String dropoffAddress;
    
    private String paymentMode;
}