// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/repository/CartItemRepository.java
package com.example.sairam_tea_backend.repository;

import com.example.sairam_tea_backend.model.Cart;
import com.example.sairam_tea_backend.model.CartItem;
import com.example.sairam_tea_backend.model.Product;
import com.example.sairam_tea_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByUserAndProductId(User user, Long productId);
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);


    List<CartItem> findByUser(User user);
    void deleteByUser(User user);
}
