package com.example.sairam_tea_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"cart", "user"}) // Exclude cart AND user to prevent infinite loop in toString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // IMPORTANT: This is the missing link!
    // Many-to-one relationship with Cart. A CartItem must belong to a Cart.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false) // Foreign key to the shopping_carts table
    @EqualsAndHashCode.Exclude
    private Cart cart; // <--- This field name 'cart' is what 'mappedBy' in Cart.java refers to.

    // A CartItem also belongs to a User, but it should typically be through the Cart.
    // Having a direct `user` link in CartItem AND `cart` link to Cart (which has a user)
    // might be redundant or indicate a slightly different design.
    // If the cart is always associated with a user, you might not strictly need `user` directly here.
    // However, if a CartItem can exist without a Cart (e.g., as a standalone favorite), then it makes sense.
    // Given the context, I'll keep it for now but note the potential redundancy.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false) // user_id cannot be null. Make it read-only if managing via Cart.
    @EqualsAndHashCode.Exclude
    private User user; // This is the field that CartItem maps to User

    // You might want to consider referencing the actual Product entity here
    // instead of just storing its ID, name, price etc. This allows for direct
    // access to product details and avoids data duplication/inconsistency.
    // Example:
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Product product; // Assuming you have a Product entity

    // If using the Product entity directly, these fields might become redundant:
    // @Column(nullable = false)
    // private Long productId; // Redundant if @ManyToOne Product is used

    // @Column(nullable = false)
    // private String productName; // Redundant
    // @Column(nullable = false)
    // private double productPrice; // Redundant
    // private String productImageUrl; // Redundant
    // private int productStock; // Redundant

    @Column(nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
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

    // Constructor to easily create CartItem with Product
    // public CartItem(Cart cart, User user, Product product, int quantity) {
    //     this.cart = cart;
    //     this.user = user;
    //     this.product = product;
    //     this.quantity = quantity;
    //     this.productPrice = product.getPrice(); // Assuming Product has getPrice()
    //     this.productName = product.getName(); // Assuming Product has getName()
    //     this.productImageUrl = product.getImageUrl(); // Assuming Product has getImageUrl()
    // }
}