// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/dto/ReviewDTO.java
package com.example.sairam_tea_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long productId;
    private Long userId; // Include userId for potential future use (e.g., "my reviews")
    private String username; // Display username directly
    private int rating;
    private String reviewText;
    private LocalDateTime createdAt;
}
