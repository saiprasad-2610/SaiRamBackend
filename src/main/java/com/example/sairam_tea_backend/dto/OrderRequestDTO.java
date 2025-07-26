// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/dto/OrderRequestDTO.java
package com.example.sairam_tea_backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List; // If you plan to pass specific product IDs and quantities

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String productsOrdered; // This will be the string representation from the cart
    private String deliveryAddress;
    private String specialInstructions;
    private String paymentMethod;
    private double amount;
    private Long userId; // Optional, will be set by the backend if user is logged in
}
