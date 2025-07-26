package com.example.sairam_tea_backend.controller;

import com.example.sairam_tea_backend.dto.AuthResponse;
import com.example.sairam_tea_backend.dto.LoginRequest;
import com.example.sairam_tea_backend.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allow CORS for development
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);

            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Create response
            AuthResponse authResponse = new AuthResponse(
                    userDetails.getUsername(),
                    token,
                    userDetails.getAuthorities()
            );

            return ResponseEntity.ok(authResponse);

        } catch (BadCredentialsException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid username or password");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest registerRequest) {
        // You can implement user registration here
        // For now, redirect to your existing UserController
        Map<String, String> response = new HashMap<>();
        response.put("message", "Please use /api/users/register for registration");
        return ResponseEntity.badRequest().body(response);
    }
}