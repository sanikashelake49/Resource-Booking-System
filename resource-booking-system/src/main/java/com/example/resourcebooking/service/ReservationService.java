package com.example.resourcebooking.service;

import com.example.resourcebooking.dto.PageResponse;
import com.example.resourcebooking.dto.ReservationRequest;
import com.example.resourcebooking.dto.ReservationResponse;
import com.example.resourcebooking.entity.Reservation;
import com.example.resourcebooking.entity.Resource;
import com.example.resourcebooking.entity.User;
import com.example.resourcebooking.enums.ReservationStatus;
import com.example.resourcebooking.enums.Role;
import com.example.resourcebooking.exception.BadRequestException;
import com.example.resourcebooking.exception.BookingConflictException;
import com.example.resourcebooking.exception.ReservationNotFoundException;
import com.example.resourcebooking.exception.ResourceNotFoundException;
import com.example.resourcebooking.repository.ReservationRepository;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.specification.ReservationSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    /**
     * Creates a reservation for the CURRENTLY AUTHENTICATED user.
     * The caller (controller) passes in the resolved User entity taken
     * from the JWT principal - never from the request body.
     */
    public ReservationResponse create(ReservationRequest request, User currentUser) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("startTime must be before endTime");
        }

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found with id: " + request.getResourceId()));

        List<Reservation> overlaps = reservationRepository.findOverlapping(
                resource.getId(), request.getStartTime(), request.getEndTime(), ACTIVE_STATUSES, null);
        if (!overlaps.isEmpty()) {
            throw new BookingConflictException(
                    "Resource is already booked for an overlapping time range");
        }

        ReservationStatus status = ReservationStatus.PENDING;
        if (currentUser.getRole() == Role.ADMIN && request.getStatus() != null) {
            status = request.getStatus();
        }

        Reservation reservation = Reservation.builder()
                .resource(resource)
                .user(currentUser)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(request.getPrice())
                .status(status)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Created reservation id={} for user={} resource={}", saved.getId(),
                currentUser.getUsername(), resource.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getAll(User currentUser,
                                                      ReservationStatus status,
                                                      BigDecimal minPrice,
                                                      BigDecimal maxPrice,
                                                      Pageable pageable) {
        // USERS only ever see their own reservations - ownership is enforced
        // here server-side and CANNOT be bypassed via query params.
        Long ownerUserId = currentUser.getRole() == Role.ADMIN ? null : currentUser.getId();

        var spec = ReservationSpecification.build(status, minPrice, maxPrice, ownerUserId);
        Page<ReservationResponse> page = reservationRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.fromPage(page);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getById(Long id, User currentUser) {
        Reservation reservation = findEntity(id);
        assertOwnershipOrAdmin(reservation, currentUser);
        return toResponse(reservation);
    }

    public ReservationResponse update(Long id, ReservationRequest request, User currentUser) {
        Reservation reservation = findEntity(id);
        assertOwnershipOrAdmin(reservation, currentUser);

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("startTime must be before endTime");
        }

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found with id: " + request.getResourceId()));

        List<Reservation> overlaps = reservationRepository.findOverlapping(
                resource.getId(), request.getStartTime(), request.getEndTime(), ACTIVE_STATUSES, reservation.getId());
        if (!overlaps.isEmpty()) {
            throw new BookingConflictException(
                    "Resource is already booked for an overlapping time range");
        }

        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(request.getPrice());

        // Only ADMIN may change reservation status; a USER updating their
        // own reservation keeps its current status regardless of request body.
        if (currentUser.getRole() == Role.ADMIN && request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
        }

        Reservation saved = reservationRepository.save(reservation);
        log.info("Updated reservation id={} by user={}", saved.getId(), currentUser.getUsername());
        return toResponse(saved);
    }

    public void delete(Long id, User currentUser) {
        Reservation reservation = findEntity(id);
        assertOwnershipOrAdmin(reservation, currentUser);
        reservationRepository.delete(reservation);
        log.info("Deleted reservation id={} by user={}", id, currentUser.getUsername());
    }

    private void assertOwnershipOrAdmin(Reservation reservation, User currentUser) {
        boolean isOwner = reservation.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            // Secure strategy: do not reveal that the reservation exists.
            throw new AccessDeniedException("You do not have permission to access this reservation");
        }
    }

    private Reservation findEntity(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));
    }

    private ReservationResponse toResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .resourceId(r.getResource().getId())
                .resourceName(r.getResource().getName())
                .userId(r.getUser().getId())
                .username(r.getUser().getUsername())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .price(r.getPrice())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
