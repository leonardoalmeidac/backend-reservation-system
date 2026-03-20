package com.gallery.reservation.dto.request;

import com.gallery.reservation.entity.Room;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomRequest {
    
    @NotBlank(message = "Room name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    private String location;
    
    private Boolean isActive = true;
}