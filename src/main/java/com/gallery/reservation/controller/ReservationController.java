package com.gallery.reservation.controller;

import com.gallery.reservation.dto.request.ReservationRequest;
import com.gallery.reservation.dto.response.ReservationResponse;
import com.gallery.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    
    private final ReservationService reservationService;
    
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.getReservationsByApplicant(
                reservationService.getCurrentUserId(userDetails.getUsername())
        ));
    }
    
    @GetMapping("/calendar")
    public ResponseEntity<List<ReservationResponse>> getCalendarReservations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(reservationService.getCalendarReservations(startDate, endDate));
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR_LEVEL_1', 'DIRECTOR_LEVEL_2')")
    public ResponseEntity<List<ReservationResponse>> getPendingApprovals() {
        return ResponseEntity.ok(reservationService.getPendingApprovals());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }
    
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.createReservation(request, userDetails.getUsername()));
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR_LEVEL_1', 'DIRECTOR_LEVEL_2')")
    public ResponseEntity<ReservationResponse> approveReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.approveReservation(id, userDetails.getUsername()));
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR_LEVEL_1', 'DIRECTOR_LEVEL_2')")
    public ResponseEntity<ReservationResponse> rejectReservation(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String notes = body.get("notes");
        return ResponseEntity.ok(reservationService.rejectReservation(id, userDetails.getUsername(), notes));
    }
    
    @PostMapping("/{id}/request-info")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR_LEVEL_1', 'DIRECTOR_LEVEL_2')")
    public ResponseEntity<ReservationResponse> requestInfo(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String notes = body.get("notes");
        return ResponseEntity.ok(reservationService.requestInfo(id, userDetails.getUsername(), notes));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, userDetails.getUsername()));
    }
}