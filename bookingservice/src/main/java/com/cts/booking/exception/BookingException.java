package com.cts.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Custom exception for business logic errors (e.g., ride already taken)
@ResponseStatus(HttpStatus.CONFLICT) // 409
public class BookingException extends RuntimeException {
    public BookingException(String message) {
        super(message);
    }
}