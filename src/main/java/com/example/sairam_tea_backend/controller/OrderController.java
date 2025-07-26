package com.example.sairam_tea_backend.controller;

import com.example.sairam_tea_backend.dto.OrderRequestDTO; // Assuming you have this DTO
import com.example.sairam_tea_backend.model.Order;
import com.example.sairam_tea_backend.model.User;
import com.example.sairam_tea_backend.service.OrderService;
import com.example.sairam_tea_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * Helper method to get the current authenticated User entity from UserDetails.
     * Throws IllegalArgumentException if user is not authenticated or not found.
     * @param userDetails The authenticated user details provided by Spring Security.
     * @return The User entity corresponding to the authenticated user.
     */
    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("User not authenticated.");
        }
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userDetails.getUsername()));
    }

    /**
     * Helper method to convert an Order entity to a frontend-friendly Map (DTO-like structure).
     * This avoids direct exposure of JPA entities and handles potential lazy loading issues.
     * @param order The Order entity to convert.
     * @return A Map representing the order data.
     */
    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", order.getId());
        orderMap.put("customerName", order.getCustomerName());
        orderMap.put("customerPhone", order.getCustomerPhone());
        orderMap.put("customerEmail", order.getCustomerEmail());
        orderMap.put("productsOrdered", order.getProductsOrdered());
        orderMap.put("deliveryAddress", order.getDeliveryAddress());
        orderMap.put("specialInstructions", order.getSpecialInstructions());
        orderMap.put("amount", order.getAmount());
        orderMap.put("orderDate", order.getOrderDate());
        orderMap.put("status", order.getStatus());
        orderMap.put("razorpayOrderId", order.getRazorpayOrderId());
        orderMap.put("razorpayPaymentId", order.getRazorpayPaymentId());
        orderMap.put("paymentMethod", order.getPaymentMethod());
        orderMap.put("createdAt", order.getCreatedAt());
        orderMap.put("updatedAt", order.getUpdatedAt());
        if (order.getUser() != null) {
            orderMap.put("userId", order.getUser().getId());
            orderMap.put("username", order.getUser().getUsername());
        } else {
            orderMap.put("userId", null);
            orderMap.put("username", "Guest");
        }
        // You might want to include orderItems here if they are part of the DTO
        // if (order.getOrderItems() != null) {
        //     orderMap.put("orderItems", order.getOrderItems().stream()
        //         .map(item -> {
        //             Map<String, Object> itemMap = new HashMap<>();
        //             itemMap.put("productId", item.getProduct().getId()); // Assuming OrderItem has a Product
        //             itemMap.put("productName", item.getProduct().getName());
        //             itemMap.put("quantity", item.getQuantity());
        //             itemMap.put("price", item.getPrice());
        //             return itemMap;
        //         })
        //         .collect(Collectors.toList()));
        // }
        return orderMap;
    }


    /**
     * Endpoint for customers to place a new order.
     * Can be accessed by authenticated users or guests (if allowed by security config).
     *
     * @param orderRequestDTO DTO containing order details from the frontend.
     * @param userDetails     Authenticated user details (optional, for logged-in users).
     * @return ResponseEntity with the created order data or an error message.
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDTO orderRequestDTO,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = null;
            if (userDetails != null) {
                user = userService.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
            }

            // Create a new Order instance from DTO
            Order newOrder = new Order();
            newOrder.setCustomerName(orderRequestDTO.getCustomerName());
            newOrder.setCustomerPhone(orderRequestDTO.getCustomerPhone());
            newOrder.setCustomerEmail(orderRequestDTO.getCustomerEmail());
            newOrder.setProductsOrdered(orderRequestDTO.getProductsOrdered()); // Still using this, consider OrderItem
            newOrder.setDeliveryAddress(orderRequestDTO.getDeliveryAddress());
            newOrder.setSpecialInstructions(orderRequestDTO.getSpecialInstructions());
            newOrder.setAmount(orderRequestDTO.getAmount());
            newOrder.setPaymentMethod(orderRequestDTO.getPaymentMethod());
            // Other fields like razorpayOrderId, razorpayPaymentId will be set on confirmation

            Order createdOrder = orderService.createOrder(newOrder, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertOrderToMap(createdOrder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating order: " + e.getMessage());
        }
    }

    /**
     * Endpoint for admin to get all orders, with optional date range filtering.
     * Requires ADMIN role.
     *
     * @param startDate Optional start date for filtering (formatYYYY-MM-DD).
     * @param endDate   Optional end date for filtering (formatYYYY-MM-DD).
     * @return ResponseEntity with a list of orders or NO_CONTENT if none found.
     */
    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = null;
            LocalDate end = null;

            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDate.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDate.parse(endDate);
            }

            List<Order> orders = orderService.getAllOrders(start, end);

            if (orders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No orders found.");
            }
            List<Map<String, Object>> orderMaps = orders.stream()
                    .map(this::convertOrderToMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderMaps);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format. Please use YYYY-MM-DD.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching orders: " + e.getMessage());
        }
    }

    /**
     * Endpoint for a logged-in user to view their own orders.
     * Requires authentication.
     *
     * @param userDetails Authenticated user details.
     * @return ResponseEntity with a list of the user's orders or NO_CONTENT if none found.
     */
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getCurrentUser(userDetails);
            List<Order> orders = orderService.getOrdersByUser(user);

            if (orders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No orders found for this user.");
            }
            List<Map<String, Object>> orderMaps = orders.stream()
                    .map(this::convertOrderToMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderMaps);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching your orders: " + e.getMessage());
        }
    }

    /**
     * Endpoint for admin to update the status of an order.
     * Requires ADMIN role.
     *
     * @param id        The ID of the order to update.
     * @param newStatus Map containing the new status string (e.g., {"status": "ORDER_CONFIRMED"}).
     * @return ResponseEntity with the updated order data or an error message.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> newStatus) {
        try {
            String statusString = newStatus.get("status");
            if (statusString == null || statusString.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New status cannot be empty.");
            }

            // Convert String to Order.OrderStatus enum
            Order.OrderStatus newOrderStatus = Order.OrderStatus.valueOf(statusString.toUpperCase()); // Convert to uppercase to match enum names

            Order updatedOrder = orderService.updateOrderStatus(id, newOrderStatus); // <--- CORRECTED LINE
            return ResponseEntity.ok(convertOrderToMap(updatedOrder));
        } catch (IllegalArgumentException e) {
            // This catch block will now also catch if valueOf fails (e.g., invalid status string)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid order ID or status: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating order status: " + e.getMessage());
        }
    }

    /**
     * Endpoint for admin to export orders to an Excel file.
     * Requires ADMIN role.
     *
     * @param startDate Optional start date for filtering (formatYYYY-MM-DD).
     * @param endDate   Optional end date for filtering (formatYYYY-MM-DD).
     * @return ResponseEntity with the Excel file as a byte array or NO_CONTENT if no orders.
     */
    @GetMapping("/export-excel")
    public ResponseEntity<?> exportOrdersToExcel(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = null;
            LocalDate end = null;

            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDate.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDate.parse(endDate);
            }

            ByteArrayInputStream bis = orderService.exportOrdersToExcel(start, end);

            if (bis == null || bis.available() == 0) {
                // Return 204 No Content if no data to export, or 200 with an empty file depending on preference
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No orders found for the specified date range to export.");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=orders.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(bis));
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format. Please use YYYY-MM-DD.");
        } catch (RuntimeException e) { // Catch RuntimeException from service for Excel errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error exporting orders to Excel: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during Excel export: " + e.getMessage());
        }
    }
}