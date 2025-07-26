// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/repository/ReviewRepository.java
package com.example.sairam_tea_backend.repository;

import com.example.sairam_tea_backend.model.Product;
import com.example.sairam_tea_backend.model.Review;
import com.example.sairam_tea_backend.model.User; // Import User model
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Import Optional

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find all reviews for a specific product
    List<Review> findByProduct(Product product);

    // Find a review by product and user (to check if a user has already reviewed a product)
    Optional<Review> findByProductAndUser(Product product, User user); // <--- NEW METHOD ADDED
}
