// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/repository/CartRepository.java
package com.example.sairam_tea_backend.repository;

import com.example.sairam_tea_backend.model.Cart;
import com.example.sairam_tea_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Find a cart by user, eagerly fetching cart items and their associated products
    @EntityGraph(attributePaths = {"cartItems", "cartItems.product"})
    Optional<Cart> findByUser(User user);
}
