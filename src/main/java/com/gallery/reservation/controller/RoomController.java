package com.gallery.reservation.controller;

import com.gallery.reservation.dto.request.RoomRequest;
import com.gallery.reservation.dto.response.RoomResponse;
import com.gallery.reservation.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    
    private final RoomService roomService;
    
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<RoomResponse>> getActiveRooms() {
        return ResponseEntity.ok(roomService.getActiveRooms());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable Long id,
            @RequestParam String start,
            @RequestParam String end) {
        
        java.time.LocalDateTime startDateTime = java.time.LocalDateTime.parse(start);
        java.time.LocalDateTime endDateTime = java.time.LocalDateTime.parse(end);
        
        boolean available = roomService.isRoomAvailable(id, startDateTime, endDateTime);
        
        return ResponseEntity.ok(Map.of(
                "roomId", id,
                "available", available,
                "startDatetime", startDateTime,
                "endDatetime", endDateTime
        ));
    }
}