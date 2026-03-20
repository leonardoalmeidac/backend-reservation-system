package com.gallery.reservation.service;

import com.gallery.reservation.dto.request.ReservationRequest;
import com.gallery.reservation.dto.response.ReservationResponse;
import com.gallery.reservation.entity.Reservation;
import com.gallery.reservation.entity.Room;
import com.gallery.reservation.entity.User;
import com.gallery.reservation.exception.BadRequestException;
import com.gallery.reservation.exception.ResourceNotFoundException;
import com.gallery.reservation.repository.ReservationRepository;
import com.gallery.reservation.repository.RoomRepository;
import com.gallery.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<ReservationResponse> getReservationsByApplicant(Long applicantId) {
        return reservationRepository.findByApplicantId(applicantId).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<ReservationResponse> getReservationsByRoom(Long roomId) {
        return reservationRepository.findByRoomId(roomId).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
        return ReservationResponse.fromEntity(reservation);
    }
    
    public List<ReservationResponse> getPendingApprovals() {
        return reservationRepository.findAllPendingApproval().stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<ReservationResponse> getCalendarReservations(LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findApprovedReservationsInDateRange(startDate, endDate).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, String applicantEmail) {
        User applicant = userRepository.findByEmail(applicantEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        if (!room.getIsActive()) {
            throw new BadRequestException("Room is not available");
        }
        
        if (request.getStartDatetime().isAfter(request.getEndDatetime())) {
            throw new BadRequestException("End datetime must be after start datetime");
        }
        
        if (request.getStartDatetime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot create reservation for past dates");
        }
        
        // Check for conflicting reservations
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                request.getRoomId(),
                request.getStartDatetime(),
                request.getEndDatetime()
        );
        
        boolean isException = !conflicts.isEmpty();
        
        Reservation reservation = Reservation.builder()
                .applicant(applicant)
                .room(room)
                .applicantName(request.getApplicantName())
                .organization(request.getOrganization())
                .email(request.getEmail())
                .phone(request.getPhone())
                .startDatetime(request.getStartDatetime())
                .endDatetime(request.getEndDatetime())
                .eventDescription(request.getEventDescription())
                .requestType(request.getRequestType())
                .status(isException ? Reservation.ReservationStatus.EXCEPTION : Reservation.ReservationStatus.VALIDATION)
                .approvalStatus(Reservation.ApprovalStatus.PENDING)
                .isException(isException)
                .build();
        
        reservation = reservationRepository.save(reservation);
        
        // Notify directors based on request type and exception status
        notifyDirectors(reservation);
        
        return ReservationResponse.fromEntity(reservation);
    }
    
    private void notifyDirectors(Reservation reservation) {
        List<User> directors = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.UserRole.DIRECTOR_LEVEL_1 || 
                            u.getRole() == User.UserRole.DIRECTOR_LEVEL_2 ||
                            u.getRole() == User.UserRole.ADMIN)
                .collect(Collectors.toList());
        
        String message = String.format(
                "New reservation request from %s for room %s. Type: %s. %s",
                reservation.getApplicantName(),
                reservation.getRoom().getName(),
                reservation.getRequestType(),
                reservation.getIsException() ? "EXCEPTION - Requires review" : "Pending validation"
        );
        
        for (User director : directors) {
            notificationService.createNotification(
                    director,
                    reservation,
                    com.gallery.reservation.entity.Notification.NotificationType.RESERVATION_CREATED,
                    com.gallery.reservation.entity.Notification.NotificationChannel.DASHBOARD,
                    message
            );
        }
    }
    
    @Transactional
    public ReservationResponse approveReservation(Long id, String reviewerEmail) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));
        
        // Check authorization based on request type
        validateApprovalAuthorization(reservation, reviewer);
        
        reservation.setStatus(Reservation.ReservationStatus.APPROVED);
        reservation.setApprovalStatus(Reservation.ApprovalStatus.APPROVED);
        reservation.setReviewer(reviewer);
        
        reservation = reservationRepository.save(reservation);
        
        // Notify applicant
        String message = String.format(
                "Your reservation for %s has been APPROVED.",
                reservation.getRoom().getName()
        );
        notificationService.createNotification(
                reservation.getApplicant(),
                reservation,
                com.gallery.reservation.entity.Notification.NotificationType.RESERVATION_APPROVED,
                com.gallery.reservation.entity.Notification.NotificationChannel.BOTH,
                message
        );
        
        return ReservationResponse.fromEntity(reservation);
    }
    
    @Transactional
    public ReservationResponse rejectReservation(Long id, String reviewerEmail, String notes) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));
        
        validateApprovalAuthorization(reservation, reviewer);
        
        reservation.setStatus(Reservation.ReservationStatus.REJECTED);
        reservation.setApprovalStatus(Reservation.ApprovalStatus.REJECTED);
        reservation.setReviewer(reviewer);
        reservation.setReviewNotes(notes);
        
        reservation = reservationRepository.save(reservation);
        
        // Notify applicant
        String message = String.format(
                "Your reservation for %s has been REJECTED. Reason: %s",
                reservation.getRoom().getName(),
                notes != null ? notes : "No reason provided"
        );
        notificationService.createNotification(
                reservation.getApplicant(),
                reservation,
                com.gallery.reservation.entity.Notification.NotificationType.RESERVATION_REJECTED,
                com.gallery.reservation.entity.Notification.NotificationChannel.BOTH,
                message
        );
        
        return ReservationResponse.fromEntity(reservation);
    }
    
    @Transactional
    public ReservationResponse requestInfo(Long id, String reviewerEmail, String notes) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));
        
        validateApprovalAuthorization(reservation, reviewer);
        
        reservation.setApprovalStatus(Reservation.ApprovalStatus.INFO_REQUESTED);
        reservation.setReviewer(reviewer);
        reservation.setReviewNotes(notes);
        
        reservation = reservationRepository.save(reservation);
        
        // Notify applicant
        String message = String.format(
                "Additional information is required for your reservation for %s. Notes: %s",
                reservation.getRoom().getName(),
                notes != null ? notes : "Please contact the administration"
        );
        notificationService.createNotification(
                reservation.getApplicant(),
                reservation,
                com.gallery.reservation.entity.Notification.NotificationType.INFO_REQUESTED,
                com.gallery.reservation.entity.Notification.NotificationChannel.BOTH,
                message
        );
        
        return ReservationResponse.fromEntity(reservation);
    }
    
    private void validateApprovalAuthorization(Reservation reservation, User reviewer) {
        // Admins can approve anything
        if (reviewer.getRole() == User.UserRole.ADMIN) {
            return;
        }
        
        // Exception requests require Director Level 2
        if (reservation.getIsException()) {
            if (reviewer.getRole() != User.UserRole.DIRECTOR_LEVEL_2) {
                throw new BadRequestException("Exception requests require Senior Director (Level 2) approval");
            }
            return;
        }
        
        // Corporate requests require Director Level 2
        if (reservation.getRequestType() == Reservation.RequestType.CORPORATE) {
            if (reviewer.getRole() != User.UserRole.DIRECTOR_LEVEL_2) {
                throw new BadRequestException("Corporate requests require Senior Director (Level 2) approval");
            }
            return;
        }
        
        // Other requests can be approved by Director Level 1
        if (reviewer.getRole() != User.UserRole.DIRECTOR_LEVEL_1) {
            throw new BadRequestException("You don't have permission to approve this request");
        }
    }
    
    @Transactional
    public ReservationResponse cancelReservation(Long id, String userEmail) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Only applicant or admin can cancel
        if (!reservation.getApplicant().getId().equals(user.getId()) && 
            user.getRole() != User.UserRole.ADMIN) {
            throw new BadRequestException("You can only cancel your own reservations");
        }
        
        if (reservation.getStatus() == Reservation.ReservationStatus.APPROVED) {
            reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
            reservation = reservationRepository.save(reservation);
            
            // Notify directors
            String message = String.format(
                    "Reservation for %s has been cancelled by %s",
                    reservation.getRoom().getName(),
                    user.getName()
            );
            List<User> directors = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == User.UserRole.DIRECTOR_LEVEL_1 || 
                                u.getRole() == User.UserRole.DIRECTOR_LEVEL_2 ||
                                u.getRole() == User.UserRole.ADMIN)
                    .collect(Collectors.toList());
            
            for (User director : directors) {
                notificationService.createNotification(
                        director,
                        reservation,
                        com.gallery.reservation.entity.Notification.NotificationType.RESERVATION_CANCELLED,
                        com.gallery.reservation.entity.Notification.NotificationChannel.DASHBOARD,
                        message
                );
            }
        } else {
            reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
            reservation = reservationRepository.save(reservation);
        }
        
        return ReservationResponse.fromEntity(reservation);
    }
    
    public Long getCurrentUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}