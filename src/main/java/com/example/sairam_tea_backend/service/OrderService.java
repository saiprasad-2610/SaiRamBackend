package com.example.sairam_tea_backend.service;

import com.example.sairam_tea_backend.model.Order;
import com.example.sairam_tea_backend.model.User;
import com.example.sairam_tea_backend.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Apache POI for Excel export
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService; // To decrease stock

    @Autowired
    private CartService cartService; // To clear cart after order

    /**
     * Creates a new order.
     *
     * @param order The order object to create.
     * @param user The user placing the order (can be null for guest).
     * @return The created Order entity.
     */
    @Transactional
    public Order createOrder(Order order, User user) {
        order.setUser(user); // Link user if provided
        order.setOrderDate(LocalDateTime.now());
        // createdAt and updatedAt are handled by @PrePersist and @PreUpdate in the Order entity
        order.setStatus(Order.OrderStatus.PAYMENT_PENDING.name()); // Initial status
        return orderRepository.save(order);
    }

    /**
     * Retrieves all orders (for admin).
     *
     * @param start Optional start date for filtering.
     * @param end Optional end date for filtering.
     * @return List of all orders, optionally filtered by date.
     */
    public List<Order> getAllOrders(LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            // Include start of day for start date, end of day for end date
            return orderRepository.findByOrderDateBetween(start.atStartOfDay(), end.atTime(23, 59, 59));
        } else {
            return orderRepository.findAll();
        }
    }

    /**
     * Retrieves orders for a specific user.
     *
     * @param user The user whose orders to retrieve.
     * @return List of orders for the user.
     */
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId The ID of the order.
     * @return Optional containing the order if found.
     */
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Updates the status of an order.
     *
     * @param orderId The ID of the order to update.
     * @param newStatus The new status as an Order.OrderStatus enum.
     * @return The updated Order entity.
     * @throws IllegalArgumentException if order not found.
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) { // <--- This method signature is correct
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        order.setStatus(newStatus.name()); // <--- Use .name() to convert enum to String for persistence
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /**
     * Confirms an order after successful payment.
     * This method should be called by the PaymentService after successful payment verification.
     * It updates the order status to ORDER_PLACED and decreases product stock.
     *
     * @param orderId The ID of the order to confirm.
     * @param razorpayOrderId Razorpay Order ID.
     * @param razorpayPaymentId Razorpay Payment ID.
     */
    @Transactional
    public void confirmOrder(Long orderId, String razorpayOrderId, String razorpayPaymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        // IMPORTANT: Compare enum directly, not String.
        // Also, check for null before comparing if `getStatus()` could return null.
        if (order.getStatus() != null && order.getStatus().equals(Order.OrderStatus.PAYMENT_PENDING.name())) {
            order.setStatus(Order.OrderStatus.ORDER_PLACED.name());
            order.setRazorpayOrderId(razorpayOrderId);
            order.setRazorpayPaymentId(razorpayPaymentId);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            // Decrease stock for items in the order
            // NOTE: This is a simplified approach. In a real app, you'd parse order.productsOrdered
            // or have a proper OrderItem entity linked to products.
            // For now, we'll assume the frontend/CartService has already validated stock.
            // If you have a proper OrderItem entity, iterate through order.getOrderItems()
            // and call productService.decreaseStock(item.getProductId(), item.getQuantity());

            // Clear the user's cart after successful order, if applicable
            if (order.getUser() != null) {
                cartService.clearCart(order.getUser());
            }

        } else {
            throw new IllegalStateException("Order " + orderId + " is not in PAYMENT_PENDING status. Current status: " + order.getStatus());
        }
    }

    /**
     * Sets an order status to PAYMENT_FAILED.
     *
     * @param orderId The ID of the order.
     */
    @Transactional
    public void failOrderPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        order.setStatus(Order.OrderStatus.PAYMENT_FAILED.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * Retrieves orders within a specific date range.
     *
     * @param startDate Start date (inclusive).
     * @param endDate End date (inclusive).
     * @return List of orders within the date range.
     */
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    /**
     * Exports orders to an Excel file.
     *
     * @param startDate Optional start date for filtering.
     * @param endDate Optional end date for filtering.
     * @return ByteArrayInputStream containing the Excel file.
     */
    public ByteArrayInputStream exportOrdersToExcel(LocalDate startDate, LocalDate endDate) {
        List<Order> orders;
        if (startDate != null && endDate != null) {
            orders = orderRepository.findByOrderDateBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        } else {
            orders = orderRepository.findAll();
        }

        String[] COLUMNs = {"Order ID", "Customer Name", "Customer Phone", "Customer Email", "Delivery Address",
                "Products Ordered", "Amount", "Order Date", "Status", "Payment Method",
                "Razorpay Order ID", "Razorpay Payment ID", "User (if any)"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Orders");

            // Header
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < COLUMNs.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(COLUMNs[col]);
            }

            // Data rows
            int rowIdx = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getCustomerName());
                row.createCell(2).setCellValue(order.getCustomerPhone());
                row.createCell(3).setCellValue(order.getCustomerEmail());
                row.createCell(4).setCellValue(order.getDeliveryAddress());
                row.createCell(5).setCellValue(order.getProductsOrdered());
                row.createCell(6).setCellValue(order.getAmount());
                row.createCell(7).setCellValue(order.getOrderDate().toString());
                row.createCell(8).setCellValue(order.getStatus());
                row.createCell(9).setCellValue(order.getPaymentMethod());
                row.createCell(10).setCellValue(order.getRazorpayOrderId());
                row.createCell(11).setCellValue(order.getRazorpayPaymentId());
                row.createCell(12).setCellValue(order.getUser() != null ? order.getUser().getUsername() : "Guest");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Fail to import data to Excel file: " + e.getMessage(), e);
        }
    }
}