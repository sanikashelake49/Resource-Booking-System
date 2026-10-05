package com.example.resourcebooking.repository;

import com.example.resourcebooking.entity.Reservation;
import com.example.resourcebooking.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    /**
     * Finds reservations on the same resource that overlap the given time
     * window and are still active (PENDING or CONFIRMED). Used to prevent
     * double-booking. Standard interval overlap check:
     * existing.start < newEnd AND existing.end > newStart
     */
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.resource.id = :resourceId " +
            "AND r.status IN :activeStatuses " +
            "AND r.startTime < :endTime " +
            "AND r.endTime > :startTime " +
            "AND (:excludeReservationId IS NULL OR r.id <> :excludeReservationId)")
    List<Reservation> findOverlapping(@Param("resourceId") Long resourceId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("activeStatuses") List<ReservationStatus> activeStatuses,
                                       @Param("excludeReservationId") Long excludeReservationId);
}
