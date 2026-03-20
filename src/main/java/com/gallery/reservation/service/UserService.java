package com.gallery.reservation.service;

import com.gallery.reservation.entity.User;
import com.gallery.reservation.exception.BadRequestException;
import com.gallery.reservation.exception.ResourceNotFoundException;
import com.gallery.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
    
    @Transactional
    public User createUser(String name, String email, String password, String phone, String organization, User.UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }
        
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .organization(organization)
                .role(role != null ? role : User.UserRole.APPLICANT)
                .build();
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateUser(Long id, String name, String phone, String organization, User.UserRole role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        if (name != null) {
            user.setName(name);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (organization != null) {
            user.setOrganization(organization);
        }
        if (role != null) {
            user.setRole(role);
        }
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }
    
    public long countUsers() {
        return userRepository.count();
    }
    
    public long countByRole(User.UserRole role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .count();
    }
}