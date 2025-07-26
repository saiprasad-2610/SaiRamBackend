// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/service/ReviewService.java
package com.example.sairam_tea_backend.service;

import com.example.sairam_tea_backend.model.Product;
import com.example.sairam_tea_backend.model.Review;
import com.example.sairam_tea_backend.model.User;
import com.example.sairam_tea_backend.repository.ProductRepository;
import com.example.sairam_tea_backend.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Creates a new review for a product.
     *
     * @param productId The ID of the product being reviewed.
     * @param user The user submitting the review.
     * @param review The review object containing rating and text.
     * @return The created Review entity.
     * @throws IllegalArgumentException if product not found, rating is invalid, or user has already reviewed.
     */
    @Transactional
    public Review createReview(Long productId, User user, Review review) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        // Check if the user has already reviewed this product
        Optional<Review> existingReview = reviewRepository.findByProductAndUser(product, user);
        if (existingReview.isPresent()) {
            throw new IllegalArgumentException("You have already submitted a review for this product.");
        }

        review.setProduct(product);
        review.setUser(user);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        updateProductRating(product); // Update product's average rating and review count
        return savedReview;
    }

    /**
     * Retrieves all reviews for a specific product.
     *
     * @param productId The ID of the product.
     * @return A list of reviews for the product.
     */
    public List<Review> getReviewsByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        return reviewRepository.findByProduct(product);
    }

    /**
     * Updates an existing review.
     *
     * @param reviewId The ID of the review to update.
     * @param user The user who owns the review.
     * @param updatedReview The updated review object.
     * @return The updated Review entity.
     * @throws IllegalArgumentException if review not found, user is not owner, or rating is invalid.
     */
    @Transactional
    public Review updateReview(Long reviewId, User user, Review updatedReview) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        if (!existingReview.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to update this review.");
        }

        if (updatedReview.getRating() < 1 || updatedReview.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        existingReview.setRating(updatedReview.getRating());
        existingReview.setReviewText(updatedReview.getReviewText());
        existingReview.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(existingReview);
        updateProductRating(existingReview.getProduct()); // Update product's average rating and review count
        return savedReview;
    }

    /**
     * Deletes a review.
     *
     * @param reviewId The ID of the review to delete.
     * @param user The user who owns the review.
     * @throws IllegalArgumentException if review not found or user is not owner.
     */
    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        if (!existingReview.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to delete this review.");
        }

        Product product = existingReview.getProduct(); // Get product before deleting review
        reviewRepository.delete(existingReview);
        updateProductRating(product); // Update product's average rating and review count
    }

    /**
     * Helper method to calculate and update the average rating and review count for a product.
     * This method is called after a review is created, updated, or deleted.
     *
     * @param product The product whose rating needs to be updated.
     */
    private void updateProductRating(Product product) {
        List<Review> reviews = reviewRepository.findByProduct(product);
        int reviewCount = reviews.size();
        double totalRating = reviews.stream().mapToInt(Review::getRating).sum();
        double averageRating = reviewCount > 0 ? totalRating / reviewCount : 0.0;

        product.setAverageRating(averageRating);
        product.setReviewCount(reviewCount);
        productRepository.save(product); // Save the updated product
    }
}
