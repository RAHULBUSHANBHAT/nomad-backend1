package com.cts.rider.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletDto {
    private String id;
    private String userId;
    private double balance;
    private String status;
}