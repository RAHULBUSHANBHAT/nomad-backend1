package com.cts.driver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Custom exception for when an offer is already taken
@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict
public class OfferException extends RuntimeException {
    public OfferException(String message) {
        super(message);
    }
}