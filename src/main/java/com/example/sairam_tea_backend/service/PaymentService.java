// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/service/PaymentService.java
package com.example.sairam_tea_backend.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final String razorpayKeyId;
    private final String razorpayKeySecret;
    private final OrderService orderService;

    // Using constructor injection for dependencies and @Value fields
    // This makes the dependencies explicit and the class more testable
    public PaymentService(
            @Value("${razorpay.key_id}") String razorpayKeyId, // Changed to underscore
            @Value("${razorpay.key_secret}") String razorpayKeySecret, // Changed to underscore
            OrderService orderService) { // Spring automatically injects OrderService
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
        this.orderService = orderService;
        logger.info("Razorpay Key ID: {}", razorpayKeyId);
        // logger.info("Razorpay Key Secret: {}", razorpayKeySecret); // Do NOT log secrets in production!
    }

    /**
     * Creates a Razorpay order.
     *
     * @param appOrderId Your internal order ID.
     * @param amount The total amount for the order in INR.
     * @return The Razorpay Order object.
     * @throws RazorpayException if there's an error creating the order.
     */
    public com.razorpay.Order createRazorpayOrder(Long appOrderId, double amount) throws RazorpayException {
        logger.info("Attempting to create Razorpay order for internal order ID: {} with amount: {}", appOrderId, amount);
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amount in the smallest currency unit (paise for INR)
            orderRequest.put("amount", (int) (amount));
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_rcptid_" + appOrderId); // Unique receipt ID for your reference
            orderRequest.put("payment_capture", 1); // Automatically capture payment

            // Add notes to link Razorpay order to your internal order
            JSONObject notes = new JSONObject();
            notes.put("app_order_id", appOrderId);
            orderRequest.put("notes", notes);

            com.razorpay.Order createdOrder = razorpay.orders.create(orderRequest);
            logger.info("Successfully created Razorpay order with ID: {}", (Object) createdOrder.get("id"));
            return createdOrder;
        } catch (RazorpayException e) {
            logger.error("Error creating Razorpay order: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating Razorpay order: {}", e.getMessage(), e);
            throw new RazorpayException("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Verifies a Razorpay payment signature.
     *
     * @param razorpayPaymentId The payment ID received from Razorpay.
     * @param razorpayOrderId The order ID received from Razorpay.
     * @param razorpaySignature The signature received from Razorpay.
     * @param appOrderId Your internal order ID.
     * @return true if the signature is valid, false otherwise.
     */
    @Transactional
    public boolean verifyPaymentSignature(String razorpayPaymentId, String razorpayOrderId, String razorpaySignature, Long appOrderId) {
        logger.info("Verifying Razorpay payment signature for internal order ID: {}", appOrderId);
        try {
            // Note: RazorpayClient is not strictly needed for signature verification
            // but if you were to fetch payment details here, you would need it.
            // Utils.verifyPaymentSignature is a static method.
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isVerified = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (isVerified) {
                logger.info("Payment signature verified successfully for internal order ID: {}", appOrderId);
                orderService.confirmOrder(appOrderId, razorpayOrderId, razorpayPaymentId);
            } else {
                logger.warn("Payment signature verification failed for internal order ID: {}", appOrderId);
                orderService.failOrderPayment(appOrderId);
            }
            return isVerified;
        } catch (RazorpayException e) {
            logger.error("RazorpayException during payment signature verification for internal order ID {}: {}", appOrderId, e.getMessage(), e);
            // Mark order as failed if verification process itself throws an exception
            orderService.failOrderPayment(appOrderId);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during payment verification for internal order ID {}: {}", appOrderId, e.getMessage(), e);
            orderService.failOrderPayment(appOrderId);
            return false;
        }
    }

    /**
     * Retrieves a payment by its ID from Razorpay (optional, for auditing).
     * @param paymentId The Razorpay payment ID.
     * @return The Razorpay Payment object.
     * @throws RazorpayException
     */
    public com.razorpay.Payment fetchRazorpayPayment(String paymentId) throws RazorpayException {
        logger.info("Fetching Razorpay payment details for payment ID: {}", paymentId);
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        com.razorpay.Payment payment = razorpay.payments.fetch(paymentId);
        logger.info("Successfully fetched Razorpay payment ID: {}", (Object) payment.get("id"));
        return payment;
    }
}