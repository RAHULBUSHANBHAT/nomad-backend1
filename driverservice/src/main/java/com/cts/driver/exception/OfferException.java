package com.cts.driver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OfferException extends RuntimeException {
    public OfferException(String message) {
        super(message);
    }
}