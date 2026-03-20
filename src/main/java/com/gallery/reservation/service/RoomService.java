package com.gallery.reservation.service;

import com.gallery.reservation.dto.request.RoomRequest;
import com.gallery.reservation.dto.response.RoomResponse;
import com.gallery.reservation.entity.Reservation;
import com.gallery.reservation.entity.Room;
import com.gallery.reservation.exception.BadRequestException;
import com.gallery.reservation.exception.ResourceNotFoundException;
import com.gallery.reservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gallery.reservation.repository.ReservationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<RoomResponse> getActiveRooms() {
        return roomRepository.findByIsActiveTrue().stream()
                .map(RoomResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        return RoomResponse.fromEntity(room);
    }
    
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByName(request.getName())) {
            throw new BadRequestException("Room with this name already exists");
        }
        
        Room room = Room.builder()
                .name(request.getName())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .location(request.getLocation())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        roomRepository.save(room);
        return RoomResponse.fromEntity(room);
    }
    
    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        
        if (request.getName() != null && !request.getName().equals(room.getName())) {
            if (roomRepository.existsByName(request.getName())) {
                throw new BadRequestException("Room with this name already exists");
            }
            room.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }
        if (request.getCapacity() != null) {
            room.setCapacity(request.getCapacity());
        }
        if (request.getLocation() != null) {
            room.setLocation(request.getLocation());
        }
        if (request.getIsActive() != null) {
            room.setIsActive(request.getIsActive());
        }
        
        roomRepository.save(room);
        return RoomResponse.fromEntity(room);
    }
    
    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        room.setIsActive(false);
        roomRepository.save(room);
    }
    
    public boolean isRoomAvailable(Long roomId, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(roomId, start, end);
        return conflicts.isEmpty();
    }
}