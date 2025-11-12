package com.cts.wallet.repository;

import com.cts.wallet.model.RideTransaction;
import com.cts.wallet.model.TransactionType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class TransactionSpecification {

    public Specification<RideTransaction> hasType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.equals("all") || type.isEmpty()) {
                return cb.conjunction();
            }
            
            try {
                // 2. Convert the string to the *BookingStatus* enum
                TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
                
                // 3. Filter on the 'bookingStatus' field in your Transaction entity
                return cb.equal(root.get("type"), transactionType); 
            } catch (IllegalArgumentException e) {
                // Frontend sent an invalid status (like "pickup"), so just return all
                return cb.conjunction();
            }
        };
    }

    public Specification<RideTransaction> hasBookingId(String bookingId) {
        return (root, query, cb) -> {
            if (bookingId == null || bookingId.isEmpty()) {
                return cb.conjunction();
            }
            // 'bookingId' is the field in your Transaction entity
            return cb.like(root.get("related_booking_id"), "%" + bookingId + "%");
        };
    }

    public Specification<RideTransaction> hasDate(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            // 'timestamp' is the field from your component's logic
            return cb.between(root.get("timestamp"), date.atStartOfDay(), date.atTime(LocalTime.MAX));
        };
    }

    public Specification<RideTransaction> hasFare(String filter, Double value) {
        return (root, query, cb) -> {
            if (filter == null || value == null || filter.equals("all")) {
                return cb.conjunction();
            }
            // 'totalFare' is the field in your Transaction entity
            return switch (filter) {
                case "equal" -> cb.equal(root.get("amount"), value);
                case "greater" -> cb.greaterThanOrEqualTo(root.get("amount"), value);
                case "less" -> cb.lessThanOrEqualTo(root.get("amount"), value);
                default -> cb.conjunction();
            };
        };
    }
}