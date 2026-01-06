package org.akashbag.ecommerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.akashbag.ecommerce.dto.request.ReviewRequest;
import org.akashbag.ecommerce.enums.OrderStatus;
import org.akashbag.ecommerce.enums.Role;
import org.akashbag.ecommerce.exception.customException.ResourceNotFound;
import org.akashbag.ecommerce.model.Order;
import org.akashbag.ecommerce.model.Product;
import org.akashbag.ecommerce.model.Review;
import org.akashbag.ecommerce.model.User;
import org.akashbag.ecommerce.repository.OrderRepository; // ✅ Need this!
import org.akashbag.ecommerce.repository.ProductRepository;
import org.akashbag.ecommerce.repository.ReviewRepository;
import org.akashbag.ecommerce.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Review addReview(String productId, ReviewRequest request) {
        // 1. Get Current User
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.getUserByEmail(email); // Changed to standard findByEmail
        if (user == null) throw  new ResourceNotFound("User not found");

        // 2. Verify Product Exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound("Product not found with id: " + productId));

        // 3. Check: Has user ALREADY reviewed THIS product?
        // (Don't just check existsByUserId, check both UserID AND ProductID)
        boolean alreadyReviewed = reviewRepository.existsByUserIdAndProductId(user.getId(), productId);
        if (alreadyReviewed) {
            throw new IllegalArgumentException("You have already reviewed this product.");
        }

        // 4. Check: Has user PURCHASED this product?
        // We look for any order by this user that contains this item and is DELIVERED.
        List<Order> userOrders = orderRepository.findAllByUserId(user.getId());

        boolean hasPurchased = userOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED) // Only verified purchases
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProductId().equals(productId));

        if (!hasPurchased) {
            throw new IllegalArgumentException("You can only review products you have purchased and received.");
        }

        // 5. Create & Save Review
        Review review = Review.builder()
                .userId(user.getId())
                .productId(productId)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // 6. Recalculate Stats
        updateProductStatistics(productId);

        log.info("Review added for product: {}", productId);
        return savedReview;
    }

    @Transactional
    public void deleteReview(String reviewId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.getUserByEmail(authentication.getName());
        if (user == null) throw  new ResourceNotFound("User not found");

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFound("Review not found"));

        String productId = review.getProductId();

        // Allow deletion if User owns the review OR User is Admin
        if (!review.getUserId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("You are not allowed to delete this review");
        }

        reviewRepository.delete(review);
        updateProductStatistics(productId);

        log.info("Review deleted for product: {}", productId);
    }

    public List<Review> getReviewsByProduct(String productId) {
        return reviewRepository.findByProductId(productId);
    }

    private void updateProductStatistics(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound("Product not found"));

        List<Review> reviews = reviewRepository.findByProductId(productId);

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        double roundedAverage = Math.round(average * 10.0) / 10.0;

        product.setAverageRating(roundedAverage);
        product.setTotalReviews(reviews.size());


        productRepository.save(product);
    }
}