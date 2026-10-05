package com.example.resourcebooking.controller;

import com.example.resourcebooking.dto.PageResponse;
import com.example.resourcebooking.dto.ReservationRequest;
import com.example.resourcebooking.dto.ReservationResponse;
import com.example.resourcebooking.entity.User;
import com.example.resourcebooking.enums.ReservationStatus;
import com.example.resourcebooking.service.ReservationService;
import com.example.resourcebooking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Reservation ownership rule (critical security requirement):
 * The current user is ALWAYS resolved from the JWT-authenticated
 * Authentication principal (userService.getCurrentUser), never from
 * any userId supplied in the request body or query string.
 */
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Reservation CRUD API")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Create a reservation for the authenticated user")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request,
                                                        Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(request, currentUser));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "List reservations - ADMIN sees all, USER sees only their own")
    public ResponseEntity<PageResponse<ReservationResponse>> getAll(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(page = 0, size = 10, sort = "createdAt") Pageable pageable,
            Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        return ResponseEntity.ok(reservationService.getAll(currentUser, status, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Get a reservation by id (owner or ADMIN only)")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        return ResponseEntity.ok(reservationService.getById(id, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Update a reservation (owner or ADMIN only)")
    public ResponseEntity<ReservationResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody ReservationRequest request,
                                                        Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        return ResponseEntity.ok(reservationService.update(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Delete a reservation (owner or ADMIN only)")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        reservationService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
