// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/dto/UserUpdateDTO.java
package com.example.sairam_tea_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// DTO for updating user profile details (excluding password)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    private String fullName;
    private String email;
    private String phoneNumber;
}
