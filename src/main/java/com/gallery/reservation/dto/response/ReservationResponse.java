package com.gallery.reservation.dto.response;

import com.gallery.reservation.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    
    private Long id;
    private Long applicantId;
    private String applicantName;
    private String organization;
    private String email;
    private String phone;
    private Long roomId;
    private String roomName;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String eventDescription;
    private Reservation.RequestType requestType;
    private Reservation.ReservationStatus status;
    private Reservation.ApprovalStatus approvalStatus;
    private Long reviewerId;
    private String reviewerName;
    private String reviewNotes;
    private Boolean isException;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ReservationResponse fromEntity(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .applicantId(reservation.getApplicant() != null ? reservation.getApplicant().getId() : null)
                .applicantName(reservation.getApplicantName())
                .organization(reservation.getOrganization())
                .email(reservation.getEmail())
                .phone(reservation.getPhone())
                .roomId(reservation.getRoom() != null ? reservation.getRoom().getId() : null)
                .roomName(reservation.getRoom() != null ? reservation.getRoom().getName() : null)
                .startDatetime(reservation.getStartDatetime())
                .endDatetime(reservation.getEndDatetime())
                .eventDescription(reservation.getEventDescription())
                .requestType(reservation.getRequestType())
                .status(reservation.getStatus())
                .approvalStatus(reservation.getApprovalStatus())
                .reviewerId(reservation.getReviewer() != null ? reservation.getReviewer().getId() : null)
                .reviewerName(reservation.getReviewer() != null ? reservation.getReviewer().getName() : null)
                .reviewNotes(reservation.getReviewNotes())
                .isException(reservation.getIsException())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}