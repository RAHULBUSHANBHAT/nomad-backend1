package com.cts.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookingFiltersDto {

    @Size(max = 100, message = "Search term cannot exceed 100 characters")
    private String searchTerm;

    @Pattern(regexp = "^(PICKUP_ADDRESS|DROPOFF_ADDRESS|DATE|STATUS)$",
             message = "Invalid filter type")
    private String filterType;

    @AssertTrue(message = "Search value must be provided if filter type is set")
    private boolean isFareFilterValid() {
        if (this.filterType != null) {
            return this.searchTerm != null;
        }
        return true;
    }
}