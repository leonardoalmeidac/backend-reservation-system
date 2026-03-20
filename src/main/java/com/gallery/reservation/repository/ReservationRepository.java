package com.gallery.reservation.repository;

import com.gallery.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByApplicantId(Long applicantId);
    
    List<Reservation> findByRoomId(Long roomId);
    
    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId AND r.status IN ('PENDING', 'VALIDATION', 'APPROVED') " +
           "AND r.startDatetime < :endDatetime AND r.endDatetime > :startDatetime")
    List<Reservation> findConflictingReservations(
            @Param("roomId") Long roomId,
            @Param("startDatetime") LocalDateTime startDatetime,
            @Param("endDatetime") LocalDateTime endDatetime
    );
    
    @Query("SELECT r FROM Reservation r WHERE r.status IN ('PENDING', 'VALIDATION', 'EXCEPTION')")
    List<Reservation> findPendingApprovals();
    
    @Query("SELECT r FROM Reservation r WHERE r.approvalStatus = 'PENDING' " +
           "AND r.status IN ('PENDING', 'VALIDATION', 'EXCEPTION')")
    List<Reservation> findAllPendingApproval();
    
    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId " +
           "AND r.status IN ('PENDING', 'VALIDATION', 'APPROVED') " +
           "AND r.startDatetime >= :startDate AND r.startDatetime <= :endDate")
    List<Reservation> findByRoomIdAndDateRange(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT r FROM Reservation r WHERE r.status = 'APPROVED' " +
           "AND r.startDatetime >= :startDate AND r.startDatetime <= :endDate")
    List<Reservation> findApprovedReservationsInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}