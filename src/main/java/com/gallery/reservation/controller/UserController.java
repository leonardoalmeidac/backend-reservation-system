package com.gallery.reservation.controller;

import com.gallery.reservation.entity.User;
import com.gallery.reservation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> request) {
        User.UserRole role = request.get("role") != null ? 
                User.UserRole.valueOf(request.get("role")) : User.UserRole.APPLICANT;
        
        return ResponseEntity.ok(userService.createUser(
                request.get("name"),
                request.get("email"),
                request.get("password"),
                request.get("phone"),
                request.get("organization"),
                role
        ));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User.UserRole role = request.get("role") != null ? 
                User.UserRole.valueOf(request.get("role")) : null;
        
        return ResponseEntity.ok(userService.updateUser(
                id,
                request.get("name"),
                request.get("phone"),
                request.get("organization"),
                role
        ));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}