package com.example.resourcebooking.dto;

import com.example.resourcebooking.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * NOTE: This DTO intentionally does NOT contain a userId field.
 * The reservation owner is always derived from the authenticated
 * JWT principal on the server side (see ReservationService), never
 * from client input. This prevents ownership spoofing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

    @NotNull(message = "resourceId is required")
    private Long resourceId;

    @NotNull(message = "startTime is required")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    private LocalDateTime endTime;

    @NotNull(message = "price is required")
    @PositiveOrZero(message = "price must not be negative")
    private BigDecimal price;

    /**
     * Optional. Only respected when the authenticated caller is ADMIN.
     * Ignored (forced to PENDING) for regular USER-created reservations.
     */
    private ReservationStatus status;
}
