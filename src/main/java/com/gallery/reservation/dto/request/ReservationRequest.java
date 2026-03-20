package com.gallery.reservation.dto.request;

import com.gallery.reservation.entity.Reservation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotBlank(message = "Applicant name is required")
    private String applicantName;
    
    private String organization;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    
    @NotNull(message = "Start datetime is required")
    private LocalDateTime startDatetime;
    
    @NotNull(message = "End datetime is required")
    private LocalDateTime endDatetime;
    
    private String eventDescription;
    
    @NotNull(message = "Request type is required")
    private Reservation.RequestType requestType;
}