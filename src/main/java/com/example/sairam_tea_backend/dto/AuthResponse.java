package com.example.sairam_tea_backend.dto;

import java.util.Collection;

public class AuthResponse {
    private String username;
    private String token;
    private Collection<?> roles;
    private String message;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String username, String token, Collection<?> roles) {
        this.username = username;
        this.token = token;
        this.roles = roles;
        this.message = "Login successful";
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Collection<?> getRoles() {
        return roles;
    }

    public void setRoles(Collection<?> roles) {
        this.roles = roles;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}