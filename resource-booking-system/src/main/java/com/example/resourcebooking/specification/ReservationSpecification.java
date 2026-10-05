package com.example.resourcebooking.specification;

import com.example.resourcebooking.entity.Reservation;
import com.example.resourcebooking.enums.ReservationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Builds dynamic WHERE clauses for reservation filtering
 * (status, minPrice, maxPrice, ownership by user) without needing
 * a dedicated repository method for every filter combination.
 */
public class ReservationSpecification {

    private ReservationSpecification() {
    }

    public static Specification<Reservation> hasStatus(ReservationStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Reservation> hasMinPrice(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Reservation> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Reservation> belongsToUser(Long userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Reservation> build(ReservationStatus status,
                                                     BigDecimal minPrice,
                                                     BigDecimal maxPrice,
                                                     Long ownerUserId) {
        return Specification.where(hasStatus(status))
                .and(hasMinPrice(minPrice))
                .and(hasMaxPrice(maxPrice))
                .and(belongsToUser(ownerUserId));
    }
}
