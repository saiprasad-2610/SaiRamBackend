// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/dto/PasswordChangeDTO.java
package com.example.sairam_tea_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// DTO for changing user password
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDTO {
    private String oldPassword;
    private String newPassword;
}
