// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/controller/ReviewController.java
package com.example.sairam_tea_backend.controller;

import com.example.sairam_tea_backend.model.Review;
import com.example.sairam_tea_backend.model.User;
import com.example.sairam_tea_backend.service.ReviewService;
import com.example.sairam_tea_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
    public class ReviewController {

    @Autowired
    private ReviewService reviewService;

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
     * Endpoint to create a new review for a product.
     * Requires authentication.
     *
     * @param userDetails Authenticated user details.
     * @param productId The ID of the product being reviewed.
     * @param review The review object containing rating and text.
     * @return ResponseEntity with the created review or an error message.
     */
    @PostMapping("/product/{productId}")
    public ResponseEntity<?> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestBody Review review) {
        try {
            User user = getCurrentUser(userDetails);
            Review createdReview = reviewService.createReview(productId, user, review);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating review: " + e.getMessage());
        }
    }

    /**
     * Endpoint to get all reviews for a specific product.
     * Does NOT require authentication (publicly accessible).
     *
     * @param productId The ID of the product.
     * @return ResponseEntity with a list of reviews or NO_CONTENT if none found.
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProductId(@PathVariable Long productId) {
        try {
            List<Review> reviews = reviewService.getReviewsByProductId(productId);
            if (reviews.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No reviews found for this product.");
            }
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching reviews: " + e.getMessage());
        }
    }

    /**
     * Endpoint to update an existing review.
     * Requires authentication and ownership of the review.
     *
     * @param userDetails Authenticated user details.
     * @param reviewId The ID of the review to update.
     * @param review The updated review object.
     * @return ResponseEntity with the updated review or an error message.
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId,
            @RequestBody Review review) {
        try {
            User user = getCurrentUser(userDetails);
            Review updatedReview = reviewService.updateReview(reviewId, user, review);
            return ResponseEntity.ok(updatedReview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating review: " + e.getMessage());
        }
    }

    /**
     * Endpoint to delete a review.
     * Requires authentication and ownership of the review.
     *
     * @param userDetails Authenticated user details.
     * @param reviewId The ID of the review to delete.
     * @return ResponseEntity indicating success or an error message.
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId) {
        try {
            User user = getCurrentUser(userDetails);
            reviewService.deleteReview(reviewId, user);
            return ResponseEntity.ok("Review deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting review: " + e.getMessage());
        }
    }
}
