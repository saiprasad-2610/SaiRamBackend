// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/service/UserService.java
package com.example.sairam_tea_backend.service;

import com.example.sairam_tea_backend.model.User;
import com.example.sairam_tea_backend.repository.UserRepository;
import com.example.sairam_tea_backend.dto.UserUpdateDTO; // Import UserUpdateDTO
import com.example.sairam_tea_backend.dto.PasswordChangeDTO; // Import PasswordChangeDTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    // NEW: Update user profile details
    public User updateUserProfile(Long userId, UserUpdateDTO userUpdateDTO) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Update fields if provided and different
        if (userUpdateDTO.getFullName() != null && !userUpdateDTO.getFullName().isEmpty()) {
            existingUser.setFullName(userUpdateDTO.getFullName());
        }
        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().isEmpty()) {
            // Check if email is already taken by another user
            Optional<User> userWithSameEmail = userRepository.findByEmail(userUpdateDTO.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Email already registered by another user.");
            }
            existingUser.setEmail(userUpdateDTO.getEmail());
        }
        if (userUpdateDTO.getPhoneNumber() != null && !userUpdateDTO.getPhoneNumber().isEmpty()) {
            existingUser.setPhoneNumber(userUpdateDTO.getPhoneNumber());
        }

        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }

    // NEW: Change user password
    public void changeUserPassword(Long userId, PasswordChangeDTO passwordChangeDTO) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Verify old password
        if (!passwordEncoder.matches(passwordChangeDTO.getOldPassword(), existingUser.getPassword())) {
            throw new IllegalArgumentException("Incorrect old password.");
        }

        // Encode and set new password
        existingUser.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        existingUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(existingUser);
    }
}
