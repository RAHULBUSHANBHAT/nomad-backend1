package com.cts.wallet.dto;

import java.util.List;
import com.cts.wallet.model.RideTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RideTransactionInternalDto {
    private long successfulTransactions;
    private List<RideTransaction> latestTransactions;
}
