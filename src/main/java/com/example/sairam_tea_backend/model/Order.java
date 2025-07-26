    // sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/model/Order.java
    package com.example.sairam_tea_backend.model;

    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;
    import lombok.NoArgsConstructor;
    import lombok.AllArgsConstructor;
    import lombok.ToString;
    import lombok.EqualsAndHashCode;

    import java.time.LocalDateTime;
    import java.util.Set; // For order items

    @Entity
    @Table(name = "orders")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString(exclude = {"user", "orderItems"}) // Exclude related entities to prevent infinite loop
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public class Order {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @EqualsAndHashCode.Include
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id") // User ID can be null for guest orders
        @EqualsAndHashCode.Exclude
        private User user;

        private String customerName;
        private String customerPhone;
        private String customerEmail;
        private String productsOrdered; // Consider replacing this with a proper @OneToMany OrderItem relationship
        private String deliveryAddress;
        private String specialInstructions;
        private LocalDateTime orderDate; // Automatically set on creation
        private String status = "Pending Payment"; // Default status for new orders
        private double amount;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String paymentMethod; // <<< THIS IS THE CRUCIAL FIELD


        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        // One-to-many relationship with OrderItem
        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private Set<OrderItem> orderItems;

        @PrePersist
        protected void onCreate() {
            orderDate = LocalDateTime.now();
            createdAt = LocalDateTime.now();
            updatedAt = LocalDateTime.now();
        }

        @PreUpdate
        protected void onUpdate() {
            updatedAt = LocalDateTime.now();
        }

        // Define the OrderStatus enum directly within the Order class
        public enum OrderStatus {
            PAYMENT_PENDING, PAYMENT_FAILED, ORDER_PLACED, ORDER_CONFIRMED, PREPARING_SHIPMENT, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
        }
    }