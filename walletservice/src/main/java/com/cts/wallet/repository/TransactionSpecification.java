package com.cts.wallet.repository;

import com.cts.wallet.model.PaymentMode;
import com.cts.wallet.model.RideTransaction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class TransactionSpecification {

    public Specification<RideTransaction> hasPaymentMode(String mode) {
        return (root, query, cb) -> {
            if (mode == null || mode.equals("ALL") || mode.isEmpty()) return cb.conjunction();
            try {
                PaymentMode paymentMode = PaymentMode.valueOf(mode.toUpperCase());
                return cb.equal(root.get("paymentMode"), paymentMode); 
            } catch (IllegalArgumentException e) {
                return cb.conjunction();
            }
        };
    }

    public Specification<RideTransaction> hasBookingId(String bookingId) {
        return (root, query, cb) -> {
            if (bookingId == null || bookingId.isEmpty()) return cb.conjunction();
            return cb.like(root.get("bookingId"), "%" + bookingId + "%");
        };
    }

    public Specification<RideTransaction> hasDate(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            return cb.between(root.get("timestamp"), date.atStartOfDay(), date.atTime(LocalTime.MAX));
        };
    }

    public Specification<RideTransaction> hasFare(String filter, Double value) {
        return (root, query, cb) -> {
            if (filter == null || value == null || filter.equals("ALL")) return cb.conjunction();
            return switch (filter) {
                case "EQUAL" -> cb.equal(root.get("totalFare"), value);
                case "GREATER" -> cb.greaterThanOrEqualTo(root.get("totalFare"), value);
                case "LESS" -> cb.lessThanOrEqualTo(root.get("totalFare"), value);
                default -> cb.conjunction();
            };
        };
    }
}