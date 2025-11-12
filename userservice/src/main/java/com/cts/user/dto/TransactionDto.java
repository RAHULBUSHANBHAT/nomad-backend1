package com.cts.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDto {
    private String id;
    private double amount;
    private String type;
    private String relatedBookingId;
    private LocalDateTime timestamp;
}