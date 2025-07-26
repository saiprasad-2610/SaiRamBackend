// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/controller/CartController.java
package com.example.sairam_tea_backend.controller;

import com.example.sairam_tea_backend.model.User;
import com.example.sairam_tea_backend.service.CartService;
import com.example.sairam_tea_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List; // Import List for type checking in getCart
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("User not authenticated.");
        }
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userDetails.getUsername()));
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addProductToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        try {
            User user = getCurrentUser(userDetails);
            Map<String, Object> updatedCart = cartService.addProductToCart(user, productId, quantity);
            return ResponseEntity.ok(updatedCart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding product to cart: " + e.getMessage());
        }
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateProductQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam int quantity) {
        try {
            User user = getCurrentUser(userDetails);
            Map<String, Object> updatedCart = cartService.updateProductQuantityInCart(user, productId, quantity);
            return ResponseEntity.ok(updatedCart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating product quantity in cart: " + e.getMessage());
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeProductFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        try {
            User user = getCurrentUser(userDetails);
            Map<String, Object> updatedCart = cartService.removeProductFromCart(user, productId);
            return ResponseEntity.ok(updatedCart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error removing product from cart: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getCurrentUser(userDetails);
            Map<String, Object> emptyCart = cartService.clearCart(user);
            return ResponseEntity.ok(emptyCart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error clearing cart: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getCurrentUser(userDetails);
            Map<String, Object> cartData = cartService.getCart(user);
            // Check if the 'items' list within the map is empty
            if (cartData.get("items") instanceof List && ((List<?>) cartData.get("items")).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Cart is empty.");
            }
            return ResponseEntity.ok(cartData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching cart contents: " + e.getMessage());
        }
    }
}
