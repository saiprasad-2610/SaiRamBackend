// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/controller/PaymentController.java
package com.example.sairam_tea_backend.controller;

import com.example.sairam_tea_backend.service.PaymentService;
import com.razorpay.Order; // This is Razorpay's Order class
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService; // PaymentService is used here, not OrderRepository directly

    /**
     * Endpoint to create a Razorpay order.
     *
     * @param request Map containing "appOrderId" (your internal order ID) and "amount".
     * @return ResponseEntity with the Razorpay Order object.
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            Long appOrderId = Long.valueOf(request.get("appOrderId").toString());
            double amount = Double.parseDouble(request.get("amount").toString());

            Order razorpayOrder = paymentService.createRazorpayOrder(appOrderId, amount);
            // Return the Razorpay order as JSON
            return ResponseEntity.ok(razorpayOrder.toJson().toString());
        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating Razorpay order: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request data: " + e.getMessage());
        }
    }

    /**
     * Endpoint to verify Razorpay payment signature.
     *
     * @param request Map containing "razorpayPaymentId", "razorpayOrderId", "razorpaySignature", and "appOrderId".
     * @return ResponseEntity indicating success or failure of verification.
     */
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) {
        try {
            String razorpayPaymentId = request.get("razorpayPaymentId");
            String razorpayOrderId = request.get("razorpayOrderId");
            String razorpaySignature = request.get("razorpaySignature");
            Long appOrderId = Long.valueOf(request.get("appOrderId"));

            boolean isVerified = paymentService.verifyPaymentSignature(razorpayPaymentId, razorpayOrderId, razorpaySignature, appOrderId);

            if (isVerified) {
                return ResponseEntity.ok("Payment verified successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during payment verification: " + e.getMessage());
        }
    }
}
