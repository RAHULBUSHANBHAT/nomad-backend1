package com.cts.wallet.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TransactionFilterDto {

    @Size(max = 100, message = "Search term cannot exceed 100 characters")
    private String searchTerm;

    @Pattern(regexp = "^(ALL|WALLET|CASH)$",
             message = "Invalid payment mode")
    private String paymentMode;

    @Pattern(regexp = "^(ALL|EQUAL|GREATER|LESS)$", 
             message = "Invalid fare filter. Must be 'equal', 'greater', or 'less'")
    private String fareFilter;

    @Positive(message = "Fare value must be a positive number")
    private Double fareValue;

    @PastOrPresent(message = "Filter date cannot be in the future")
    private LocalDate dateFilter;

    @AssertTrue(message = "fareValue must be provided when a fareFilter (other than 'ALL') is set")
    private boolean isFareFilterValid() {
        if (this.fareFilter != null && !this.fareFilter.equals("ALL")) {
            return this.fareValue != null;
        }
        return true;
    }

    @AssertTrue(message = "fareFilter must be provided if fareValue is set")
    private boolean isFareValueValid() {
        if (this.fareValue != null) {
            return this.fareFilter != null;
        }
        return true;
    }
}