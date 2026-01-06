package org.akashbag.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.enums.OrderStatus;
import org.akashbag.ecommerce.enums.PaymentStatus;
import org.akashbag.ecommerce.enums.Role;
import org.akashbag.ecommerce.exception.customException.ResourceNotFound;
import org.akashbag.ecommerce.model.*;
import org.akashbag.ecommerce.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final InvoiceService invoiceService;

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElse(Cart.builder()
                        .userId(user.getId())
                        .cartItems(new ArrayList<>())
                        .totalPrice(0)
                        .build());
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.getUserByEmail(authentication.getName());
    }

    @Transactional
    public Cart addToCart(String productId, int quantity) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFound("Product not found"));

        // 1. CRITICAL: Stock Check
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock available. Only " + product.getQuantity() + " left.");
        }

        List<CartItem> cartItems = cart.getCartItems();

        Optional<CartItem> existingItem = cartItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();

            // Optional: Check if total quantity exceeds stock
            if (product.getQuantity() < (item.getQuantity() + quantity)) {
                throw new IllegalArgumentException("Cannot add more items than available stock.");
            }

            item.setQuantity(item.getQuantity() + quantity);
            item.setSubTotal(item.getUnitPrice() * item.getQuantity());
        } else {
            double price = (product.getDiscountedPrice() > 0) ? product.getDiscountedPrice() : product.getPrice();
            String image = (product.getImages() != null && !product.getImages().isEmpty())
                    ? product.getImages().getFirst() : null;

            CartItem newItem = CartItem.builder()
                    .productId(productId)
                    .productName(product.getName())
                    .image(image)
                    .unitPrice(price)
                    .subTotal(price * quantity)
                    .quantity(quantity)
                    .build();
            cartItems.add(newItem);
        }

        recalculateTotal(cart);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart decreaseItem(String productId) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);

        List<CartItem> cartItems = cart.getCartItems().stream()
                .filter(cartItem -> {
                    if (cartItem.getProductId().equals(productId) &&  cartItem.getQuantity() > 0) {
                        cartItem.setQuantity(cartItem.getQuantity() - 1);
                        cartItem.setSubTotal(cartItem.getUnitPrice() * cartItem.getQuantity());
                    }
                    return cartItem.getQuantity()>0;
                })
                .toList();
        cart.setCartItems(cartItems);
        recalculateTotal(cart);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeFromCart(String productId) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);

        List<CartItem> cartItems = cart.getCartItems().stream()
                .filter(cartItem -> !cartItem.getProductId().equals(productId))
                .toList();

        cart.setCartItems(cartItems);
        recalculateTotal(cart);
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);

        cart.setCartItems(new ArrayList<>());
        cart.setTotalPrice(0);

        cartRepository.save(cart);
    }

    public Cart getUserCart() {
        User user = getAuthenticatedUser();
        return getOrCreateCart(user);
    }

    private void recalculateTotal(Cart cart) {
        double total = cart.getCartItems().stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();
        cart.setTotalPrice(total);
    }

    @Transactional
    public Order checkout(Address shippingAddress, String paymentId, List<String> selectedProductIds) {
        User user = getAuthenticatedUser();

        if (!user.isVerified()) throw new IllegalArgumentException("verify user phone no and email to order");
        Cart cart = getOrCreateCart(user);

        // 1. Validate Cart & Selection
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        if (selectedProductIds == null || selectedProductIds.isEmpty()) {
            throw new IllegalArgumentException("No items selected for checkout");
        }

        // 2. Filter items that match the selection
        List<CartItem> itemsToCheckout = cart.getCartItems().stream()
                .filter(item -> selectedProductIds.contains(item.getProductId()))
                .toList();

        if (itemsToCheckout.isEmpty()) {
            throw new IllegalArgumentException("Selected items not found in cart");
        }

        List<OrderItem> finalOrderItems = new ArrayList<>();
        double totalOrderPrice = 0;

        // 3. Process Only Selected Items
        for (CartItem cartItem : itemsToCheckout) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFound("Product not found: " + cartItem.getProductName()));

            // Stock Check
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for: " + product.getName());
            }

            // Deduct Stock
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Create OrderItem
            double finalPrice = product.getDiscountedPrice() > 0 ? product.getDiscountedPrice() : product.getPrice();
            String image = (product.getImages() != null && !product.getImages().isEmpty())
                    ? product.getImages().getFirst() : null;

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .image(image)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(finalPrice)
                    .totalPrice(finalPrice * cartItem.getQuantity())
                    .build();

            finalOrderItems.add(orderItem);
            totalOrderPrice += orderItem.getTotalPrice();
        }

        // 4. Simulate Payment
        int paymentRandom = (int) (10 * Math.random());
        boolean isPaymentSuccess = (paymentRandom != 1);

        // 5. Create Order
        Order order = Order.builder()
                .userId(user.getId())
                .items(finalOrderItems)
                .totalPrice(totalOrderPrice)
                .shippingAddress(shippingAddress)
                .paymentId(paymentId)
                .orderStatus(isPaymentSuccess ? OrderStatus.CONFIRMED : OrderStatus.PENDING)
                .paymentStatus(isPaymentSuccess ? PaymentStatus.COMPLETED : PaymentStatus.PENDING)
                .expectedDeliveryDate(LocalDateTime.now().plusDays(7))
                .orderDate(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        createLog(savedOrder, user.getRole());
        if (isPaymentSuccess) {
            invoiceService.saveInvoice(savedOrder);
        }

        // 6. ✅ Remove ONLY purchased items from Cart
        // We keep items that were NOT in the selected list
        List<CartItem> remainingItems = cart.getCartItems().stream()
                .filter(item -> !selectedProductIds.contains(item.getProductId()))
                .toList();

        // Update Cart with remaining items
        // You usually need a mutable list for Hibernate/Spring Data, so wrap in ArrayList
        cart.setCartItems(new ArrayList<>(remainingItems));

        // 7. Recalculate Total for remaining items
        recalculateTotal(cart);
        cartRepository.save(cart);

        return savedOrder;
    }

    private void createLog(Order order, Role role) {
        OrderStatusLog orderStatusLog = OrderStatusLog.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .role(role)
                .build();
        orderStatusLogRepository.save(orderStatusLog);
    }
}