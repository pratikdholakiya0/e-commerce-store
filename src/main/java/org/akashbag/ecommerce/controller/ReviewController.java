package org.akashbag.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.request.ReviewRequest;
import org.akashbag.ecommerce.model.Review;
import org.akashbag.ecommerce.payload.ApiResponse;
import org.akashbag.ecommerce.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addReview(@RequestParam String productId,
                                                 @RequestBody ReviewRequest request) {
        Review review = reviewService.addReview(productId, request);

        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.CREATED)
                .message("Review added successfully")
                .data(review)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductReviews(@PathVariable String productId) {
        List<Review> reviews = reviewService.getReviewsByProduct(productId);

        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Reviews fetched successfully")
                .data(reviews)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse> deleteReview(@PathVariable String reviewId) {
        reviewService.deleteReview(reviewId);

        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Review deleted")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build());
    }
}