// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/controller/UserController.java
package com.example.sairam_tea_backend.controller;

import com.example.sairam_tea_backend.model.User;
import com.example.sairam_tea_backend.service.UserService;
import com.example.sairam_tea_backend.dto.UserUpdateDTO;
import com.example.sairam_tea_backend.dto.PasswordChangeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // For stream operations

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Helper method to create a User response DTO (without password)
    private User createUserResponse(User user) {
        User userResponse = new User();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFullName(user.getFullName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhoneNumber(user.getPhoneNumber());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error registering user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // User login endpoint (handled by Spring Security Basic Auth, but this endpoint can return user details)
    @PostMapping("/login")
    @PreAuthorize("isAuthenticated()") // This ensures only authenticated requests reach here
    public ResponseEntity<User> loginUser(Authentication authentication) {
        Optional<User> userOptional = userService.findByUsername(authentication.getName());
        return userOptional
                .map(this::createUserResponse) // Map to User response DTO
                .map(userResponse -> new ResponseEntity<>(userResponse, HttpStatus.OK)) // Wrap in ResponseEntity
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Provide a ResponseEntity<User> for not found
    }

    // Get current authenticated user's details
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        Optional<User> userOptional = userService.findByUsername(authentication.getName());
        return userOptional
                .map(this::createUserResponse) // Map to User response DTO
                .map(userResponse -> new ResponseEntity<>(userResponse, HttpStatus.OK)) // Wrap in ResponseEntity
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Provide a ResponseEntity<User> for not found
    }

    // NEW: Update current authenticated user's profile
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateCurrentUserProfile(@RequestBody UserUpdateDTO userUpdateDTO, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            User updatedUser = userService.updateUserProfile(userOptional.get().getId(), userUpdateDTO);
            User userResponse = createUserResponse(updatedUser); // Get DTO for response
            return new ResponseEntity<>(userResponse, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Return BAD_REQUEST with a message if update fails due to business logic
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error updating user profile: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // NEW: Change current authenticated user's password
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changeCurrentUserPassword(@RequestBody PasswordChangeDTO passwordChangeDTO, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isEmpty()) {
            return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
        }

        try {
            userService.changeUserPassword(userOptional.get().getId(), passwordChangeDTO);
            return new ResponseEntity<>("Password changed successfully!", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error changing password: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("An error occurred while changing password.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Admin-only: Get all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        // Map to response DTOs to avoid sending passwords
        List<User> userResponses = users.stream()
                .map(this::createUserResponse)
                .collect(Collectors.toList());
        return new ResponseEntity<>(userResponses, HttpStatus.OK);
    }

    // Admin-only: Get user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(this::createUserResponse) // Map to User response DTO
                .map(userResponse -> new ResponseEntity<>(userResponse, HttpStatus.OK)) // Wrap in ResponseEntity
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Provide a ResponseEntity<User> for not found
    }

    // Admin-only: Delete user by ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
