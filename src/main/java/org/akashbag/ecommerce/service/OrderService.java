package org.akashbag.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.request.OrderStatusChangeRequest;
import org.akashbag.ecommerce.dto.response.OrderResponse;
import org.akashbag.ecommerce.enums.OrderStatus;
import org.akashbag.ecommerce.enums.PaymentStatus;
import org.akashbag.ecommerce.enums.Role;
import org.akashbag.ecommerce.exception.customException.ResourceNotFound;
import org.akashbag.ecommerce.model.*;
import org.akashbag.ecommerce.repository.OrderRepository;
import org.akashbag.ecommerce.repository.OrderStatusLogRepository;
import org.akashbag.ecommerce.repository.ProductRepository;
import org.akashbag.ecommerce.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final InvoiceService invoiceService;
    
//    // 1. Create Order
//    @Transactional
//    public void createOrder(OrderRequest orderRequest) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        User user = userRepository.getUserByEmail(authentication.getName());
//
//        List<OrderItem> orderItem = orderRequest.getItems();
//        List<OrderItem> finalOrderItems = new ArrayList<>();
//
//        double totalOrderPrice = 0;
//
//        for (OrderItem item : orderItem) {
//            // 1. Fetch Product
//            Product product = productRepository.findById(item.getProductId())
//                    .orElseThrow(() -> new ResourceNotFound("Product not found with id " + item.getProductId()));
//
//            // 2. Check Stock
//            if (product.getQuantity() < item.getQuantity()) {
//                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
//            }
//
//            // 3. Deduct Stock
//            product.setQuantity(product.getQuantity() - item.getQuantity());
//            productRepository.save(product);
//
//            // 4. ✅ FIX 2: Create a "Snapshot" of the item with REAL DB PRICE
//            // We do NOT trust the 'unitPrice' sent by the frontend.
//            item.setUnitPrice(product.getPrice());
//            item.setTotalPrice(product.getPrice() * item.getQuantity());
//
//            // Add to our verified list and sum up
//            finalOrderItems.add(item);
//            totalOrderPrice += item.getTotalPrice();
//        }
//
//        int paymentStatus = (int) (10*Math.random());
//
//        Order order = Order.builder()
//                .userId(user.getId())
//                .items(finalOrderItems)
//                .orderStatus(paymentStatus!=1?OrderStatus.CONFIRMED:OrderStatus.PENDING)
//                .paymentStatus(paymentStatus!=1?PaymentStatus.COMPLETED:PaymentStatus.PENDING)
//                .paymentId(orderRequest.getPaymentId())
//                .totalPrice(totalOrderPrice)
//                .shippingAddress(orderRequest.getShippingAddress())
//                .orderDate(LocalDateTime.now())
//                .build();
//        orderRepository.save(order);
//
//        createLog(order, user.getRole());
//
//        invoiceService.saveInvoice(order);
//    }

    // 2. Cancel Order
    @Transactional
    public void cancelOrder(String orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.getUserByEmail(authentication.getName());

        // TODO: 1. Fetch Order
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFound("Order not found with id " + orderId));

        if (!order.getUserId().equals(user.getId())) throw new IllegalArgumentException("User is not authenticated to edit others orders" + user.getId());
        // TODO: 2. Ensure Status is 'PENDING' (Cannot cancel if already shipped)
        if (order.getOrderStatus() == OrderStatus.DELIVERED ||
                order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order cannot be cancelled because it is already " + order.getOrderStatus());
        }

        LocalDateTime cancelDeadline = order.getOrderDate().plusDays(1);
        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new IllegalArgumentException("Cancellation period (24 hours) has expired.");
        }
        // TODO: 3. Restore Stock (Add quantity back to products)
        List<OrderItem> orderItems = order.getItems();

        double penalty = orderItems.stream()
                .mapToDouble(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    if (product != null) {
                        product.setQuantity(product.getQuantity() + item.getQuantity());
                        productRepository.save(product);
                        return (product.getPrice() * 0.05) * item.getQuantity(); // 5% logic
                    }
                    return 0.0;
                })
                .sum();

        double finalPenalty = 0.0; // Default to FREE cancellation
        double refundAmount = order.getTotalPrice();
        if (order.getOrderStatus() == OrderStatus.SHIPPED) {
            finalPenalty = penalty;

            // Minimum Penalty Rule
            if (finalPenalty <= 100) {
                finalPenalty = 100;
            }
            // Cap Penalty at Order Total (No negative refund)
            if (finalPenalty > refundAmount) {
                finalPenalty = refundAmount;
            }

            refundAmount = refundAmount - finalPenalty;
            System.out.println("Order " + orderId + " is SHIPPED. Penalty: ₹" + finalPenalty);
        }
        // TODO: 4. Set Status -> CANCELLED & Save
        order.setUpdatedBy(user.getRole());
        order.setOrderStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            // stimulate refund to customer
            order.setCancellationPenalty(finalPenalty);
            // refund
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
        }
        orderRepository.save(order);
        createLog(order, "Cancellation req", user.getRole());
    }

    // 3. Get Specific Order
    public OrderResponse getOrderById(String orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.getUserByEmail(authentication.getName());

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFound("Order not found with id " + orderId));

        if (!order.getUserId().equals(user.getId()) && user.getRole()!=Role.ADMIN) throw new IllegalArgumentException("User is not allowed to view others order" + user.getId());
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(order.getItems())
                .status(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentId(order.getPaymentId())
                .totalPrice(order.getTotalPrice())
                .cancellationPenalty(order.getCancellationPenalty())
                .shippingAddress(order.getShippingAddress())
                .orderDate(order.getOrderDate())
                .arrivedAt(order.getArrivedAt())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .build();
    }

    // 4. Get My Orders (User History)
    public List<OrderResponse> getMyOrders() {
        // TODO: 1. Extract logged-in User ID from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.getUserByEmail(authentication.getName());

        // TODO: 2. orderRepository.findByUserId(userId)
        List<Order> allOrders = orderRepository.findByUserIdOrderByOrderDateDesc(user.getId());

        // TODO: 3. Convert List<Order> to List<OrderResponse>
        return convertToDto(allOrders);
    }

    // 5. Admin: Get All Orders
    public List<OrderResponse> getAllOrders() {
        List<Order> allOrders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate"));
        return convertToDto(allOrders);
    }

    // 6. Admin: Update Status (Shipped/Delivered)
    @Transactional
    public void updateOrderStatus(String orderId, OrderStatusChangeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.getUserByEmail(authentication.getName());

        // TODO: Fetch Order -> Update status -> Save
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFound("Order not found with id " + orderId));

        if ((request.getStatus() == OrderStatus.SHIPPED || request.getStatus() == OrderStatus.DELIVERED)
                && order.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot update status to " + request.getStatus() + " because payment is " + order.getPaymentStatus());
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order cannot be cancelled because it is already " + order.getOrderStatus());
        }
        if (request.getStatus() == OrderStatus.CANCELLED) {
            List<OrderItem> orderItems = order.getItems();
            orderItems.forEach(orderItem -> {
                Product product = productRepository.getProductById(orderItem.getProductId());
                product.setQuantity(product.getQuantity() + orderItem.getQuantity());
                productRepository.save(product);
            });
            order.setCancellationPenalty(0);

            if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
                // Refund Gateway API here
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED); // Clean up the status
            }
        }

        order.setOrderStatus(request.getStatus());
        order.setArrivedAt(request.getArrivedAt());
        order.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        orderRepository.save(order);
        createLog(order, request.getMessage(), user.getRole());
    }

    // 7. Return Product
    public void returnProduct(String orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.getUserByEmail(authentication.getName());

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFound("Order not found with id " + orderId));
        if (order.getOrderStatus()!=OrderStatus.RECEIVED_BY_CUSTOMER) throw new IllegalArgumentException("Cannot return product because order is " + order.getOrderStatus());
        LocalDateTime returnDeadline = order.getOrderDate().plusDays(7);

        if (LocalDateTime.now().isAfter(returnDeadline)) throw new IllegalArgumentException("Return period has expired. Deadline was: " + returnDeadline);
        order.setOrderStatus(OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);

        createLog(order, "", user.getRole());
    }

    private List<OrderResponse> convertToDto(List<Order> allOrders) {
        return allOrders.stream().map(item ->{
            return OrderResponse.builder()
                    .id(item.getId())
                    .userId(item.getUserId())
                    .items(item.getItems())
                    .status(item.getOrderStatus())
                    .paymentStatus(item.getPaymentStatus())
                    .paymentId(item.getPaymentId())
                    .totalPrice(item.getTotalPrice())
                    .shippingAddress(item.getShippingAddress())
                    .orderDate(item.getOrderDate())
                    .build();
        }).toList();
    }

    private void createLog(Order order, String message, Role role) {
        OrderStatusLog orderStatusLog = OrderStatusLog.builder()
                .orderId(order.getId())
                .message(message)
                .orderStatus(order.getOrderStatus())
                .role(role)
                .build();
        orderStatusLogRepository.save(orderStatusLog);
    }

    public List<OrderStatusLog> getHistory(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFound("Order not found with id " + orderId));
        return orderStatusLogRepository.findAllByOrderId(orderId);
    }
}