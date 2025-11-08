package com.cts.driver.dto;

import com.cts.driver.model.RideOfferStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RideOfferDto {
    private String id;
    private String bookingId;
    private RideOfferStatus status;
    private LocalDateTime createdAt;
    // We can add more booking details here later (e.g., pickup location)
}