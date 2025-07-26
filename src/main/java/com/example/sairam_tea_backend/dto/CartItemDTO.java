// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/dto/CartItemDTO.java
package com.example.sairam_tea_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// DTO for representing a single item in the cart
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long productId;
    private String productName;
    private String productImageUrl;
    private double productPrice;
    private int quantity;
    private int productStock; // To help frontend with stock limits
}
