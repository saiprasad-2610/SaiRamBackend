// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/dto/CartDTO.java
package com.example.sairam_tea_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

// DTO for representing the entire shopping cart
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long userId;
    private List<CartItemDTO> items;
    private double totalAmount;
}
