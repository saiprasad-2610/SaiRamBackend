package com.example.sairam_tea_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList; // Changed from HashSet to ArrayList for List
import java.util.List;

@Entity
@Table(name = "shopping_carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"cartItems", "user"}) // Exclude collections and parent from ToString to prevent lazy loading issues and loops
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Only include fields explicitly marked with @EqualsAndHashCode.Include
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Include ID for equals/hashCode
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @EqualsAndHashCode.Exclude // EXCLUDE user from equals/hashCode
    private User user;

    // One-to-many relationship with CartItem
    // mappedBy="cart" refers to the 'cart' field in the CartItem entity
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to add item to cart (manages both sides of the relationship)
    // This is good practice for managing bidirectional relationships
    public void addCartItem(CartItem item) {
        if (this.cartItems == null) {
            this.cartItems = new ArrayList<>();
        }
        this.cartItems.add(item);
        item.setCart(this); // Set the inverse side
    }

    // Helper method to remove item from cart
    public void removeCartItem(CartItem item) {
        if (this.cartItems != null) {
            this.cartItems.remove(item);
            item.setCart(null); // Remove the inverse side
        }
    }
}