package com.example.sairam_tea_backend.service;

import com.example.sairam_tea_backend.model.Cart;
import com.example.sairam_tea_backend.model.CartItem;
import com.example.sairam_tea_backend.model.Product;
import com.example.sairam_tea_backend.model.User;
import com.example.sairam_tea_backend.repository.CartItemRepository;
import com.example.sairam_tea_backend.repository.CartRepository;
import com.example.sairam_tea_backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;

    // Helper method to get or create a user's cart
    private Cart getUserCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Adds a product to the user's cart.
     * @param user The authenticated user.
     * @param productId The ID of the product to add.
     * @param quantity The quantity to add.
     * @return A map representing the updated cart details.
     */
    @Transactional
    public Map<String, Object> addProductToCart(User user, Long productId, int quantity) {
        if (user == null) {
            throw new IllegalArgumentException("User must be authenticated to add items to cart.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        Cart cart = getUserCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() + ". Available: " + product.getStockQuantity());
        }

        // --- THE ERROR LINE ---
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            if (newQuantity > product.getStockQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() + ". Available: " + product.getStockQuantity());
            }
            cartItem.setQuantity(newQuantity);
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            cartItem = new CartItem();
            cart.addCartItem(cartItem); // Use the helper to set both sides of relationship
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());
        }
        cartItemRepository.save(cartItem);
        cartRepository.save(cart); // Save the Cart to ensure relationship changes are persisted

        return getCartDetailsMap(cart);
    }

    /**
     * Updates the quantity of a product in the user's cart.
     * If newQuantity is 0 or less, the item is removed.
     * @param user The authenticated user.
     * @param productId The ID of the product.
     * @param newQuantity The new quantity.
     * @return A map representing the updated cart details.
     */
    @Transactional
    public Map<String, Object> updateProductQuantityInCart(User user, Long productId, int newQuantity) {
        if (user == null) {
            throw new IllegalArgumentException("User must be authenticated to update cart.");
        }

        Cart cart = getUserCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new IllegalArgumentException("Product not found in cart with ID: " + productId));

        if (newQuantity <= 0) {
            return removeProductFromCart(user, productId);
        }

        if (newQuantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() + ". Available: " + product.getStockQuantity());
        }

        cartItem.setQuantity(newQuantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItemRepository.save(cartItem);

        return getCartDetailsMap(cart);
    }

    /**
     * Removes a product from the user's cart.
     * @param user The authenticated user.
     * @param productId The ID of the product to remove.
     * @return A map representing the updated cart details.
     */
    @Transactional
    public Map<String, Object> removeProductFromCart(User user, Long productId) {
        if (user == null) {
            throw new IllegalArgumentException("User must be authenticated to remove items from cart.");
        }

        Cart cart = getUserCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new IllegalArgumentException("Product not found in cart with ID: " + productId));

        cart.removeCartItem(cartItem);
        cartRepository.save(cart);

        return getCartDetailsMap(cart);
    }

    /**
     * Clears all items from the user's cart.
     *
     * @param user The authenticated user.
     * @return
     */
    @Transactional
    public Map<String, Object> clearCart(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must be authenticated to clear cart.");
        }
        Cart cart = getUserCart(user);
        cart.getCartItems().clear();
        cartRepository.save(cart);
        return null;
    }

    /**
     * Retrieves the current cart for a user.
     * @param user The authenticated user (can be null for guest scenarios, though current design needs user).
     * @return A map containing cart items and total amount.
     */
    public Map<String, Object> getCart(User user) {
        if (user == null) {
            return new HashMap<String, Object>() {{
                put("items", List.of());
                put("totalAmount", 0.0);
            }};
        }

        Cart cart = getUserCart(user);
        return getCartDetailsMap(cart);
    }

    // Helper method to consolidate cart data mapping
    private Map<String, Object> getCartDetailsMap(Cart cart) {
        cart.getCartItems().size(); // Trigger lazy loading if not already loaded

        List<Map<String, Object>> itemsList = cart.getCartItems().stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("productId", item.getProduct().getId());
                    itemMap.put("productName", item.getProduct().getName());
                    itemMap.put("productPrice", item.getProduct().getPrice());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("productImageUrl", item.getProduct().getImageUrl());
                    itemMap.put("productStock", item.getProduct().getStockQuantity());
                    itemMap.put("createdAt", item.getCreatedAt());
                    itemMap.put("updatedAt", item.getUpdatedAt());
                    return itemMap;
                })
                .collect(Collectors.toList());

        double totalAmount = itemsList.stream()
                .mapToDouble(item -> (int) item.get("quantity") * (double) item.get("productPrice"))
                .sum();

        Map<String, Object> cartData = new HashMap<>();
        cartData.put("id", cart.getId());
        cartData.put("userId", cart.getUser().getId());
        cartData.put("username", cart.getUser().getUsername());
        cartData.put("items", itemsList);
        cartData.put("totalAmount", totalAmount);
        cartData.put("createdAt", cart.getCreatedAt());
        cartData.put("updatedAt", cart.getUpdatedAt());
        return cartData;
    }
}