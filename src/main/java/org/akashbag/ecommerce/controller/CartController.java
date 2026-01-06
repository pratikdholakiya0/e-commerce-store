package org.akashbag.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.request.CheckoutRequest;
import org.akashbag.ecommerce.model.Cart;
import org.akashbag.ecommerce.model.Order;
import org.akashbag.ecommerce.payload.ApiResponse;
import org.akashbag.ecommerce.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse> checkout(@RequestBody CheckoutRequest request) {

        if(request.getSelectedProductIds() == null || request.getSelectedProductIds().isEmpty()) {
            throw new IllegalArgumentException("Please select at least one item to checkout");
        }

        Order order = cartService.checkout(
                request.getShippingAddress(),
                request.getPaymentId(),
                request.getSelectedProductIds()
        );

        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Order placed successfully")
                .data(order)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private final CartService cartService;

    // 1. Add Item to Cart
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addToCart(
            @RequestParam String productId,
            @RequestParam(defaultValue = "1") int quantity) {

        Cart cart = cartService.addToCart(productId, quantity);

        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Item added to cart successfully")
                .data(cart)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/decrease/{productId}")
    public ResponseEntity<ApiResponse> decreaseQuantityFromCart(@PathVariable String productId) {
        Cart cart = cartService.decreaseItem(productId);

        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Item quantity decreased")
                .data(cart)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse> removeFromCart(@PathVariable String productId) {
        Cart cart = cartService.removeFromCart(productId);

        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Item removed from cart")
                .data(cart)
                .timestamp(LocalDateTime.now())
                .build());
    }

    // 3. Get User Cart
    @GetMapping("/")
    public ResponseEntity<ApiResponse> getUserCart() {
        Cart cart = cartService.getUserCart();

        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Cart fetched successfully")
                .data(cart)
                .timestamp(LocalDateTime.now())
                .build());
    }

    // 4. Clear Entire Cart
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart() {
        cartService.clearCart();

        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Cart cleared successfully")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build());
    }
}