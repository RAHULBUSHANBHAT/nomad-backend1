package com.cts.user.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RideTransactionInternalDto {
    private long successfulTransactions;
    private List<?> latestTransactions;
}
