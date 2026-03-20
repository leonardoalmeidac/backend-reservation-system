package com.gallery.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    
    @Column(nullable = false)
    private String applicantName;
    
    private String organization;
    
    @Column(nullable = false)
    private String email;
    
    private String phone;
    
    @Column(nullable = false)
    private LocalDateTime startDatetime;
    
    @Column(nullable = false)
    private LocalDateTime endDatetime;
    
    @Column(columnDefinition = "TEXT")
    private String eventDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType requestType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;
    
    @Column(columnDefinition = "TEXT")
    private String reviewNotes;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isException = false;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum RequestType {
        SIMPLE,
        AGREEMENT,
        EXCHANGE,
        CORPORATE
    }
    
    public enum ReservationStatus {
        PENDING,
        VALIDATION,
        EXCEPTION,
        APPROVED,
        REJECTED,
        CANCELLED
    }
    
    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED,
        INFO_REQUESTED
    }
}