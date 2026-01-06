package org.akashbag.ecommerce.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.akashbag.ecommerce.enums.OrderStatus;
import org.akashbag.ecommerce.enums.PaymentStatus;
import org.akashbag.ecommerce.model.Order;
import org.akashbag.ecommerce.model.OrderItem;
import org.akashbag.ecommerce.model.Product;
import org.akashbag.ecommerce.repository.OrderRepository;
import org.akashbag.ecommerce.repository.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductScheduler {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void cleanupPendingOrders() {
        LocalDateTime cutOffTime = LocalDateTime.now().minusMinutes(30);

        List<Order> pendingOrders = orderRepository.findAllByOrderStatusAndOrderDateBefore(
                OrderStatus.PENDING,
                cutOffTime);

        if (pendingOrders.isEmpty()) {
            return;
        }

        log.info("Found {} pending orders to process.", pendingOrders.size());

        pendingOrders.forEach(order -> {
            try {
                // We wrap EACH order in a try-catch.
                // If Order A fails, Order B still gets processed.
                processSingleOrder(order);
            } catch (Exception e) {
                // 3. Log TECHNICAL failures (Database down, NullPointer, etc.)
                log.error("Error processing expired order ID: {}", order.getId(), e);
            }
        });
    }

    private void processSingleOrder(Order order) {
        boolean paymentDone = checkPaymentStatus(order.getPaymentId());

        if (paymentDone) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            orderRepository.save(order);

            // 4. Log RECOVERY success
            log.info("Order recovered: ID {} - Payment Verified", order.getId());
            return;
        }

        // --- Cancellation Logic ---
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.FAILED);

        List<OrderItem> orderItem = order.getItems();

        orderItem.forEach(item -> {
            // Safety check: Product might be deleted
            Product product = productRepository.findById(item.getProductId()).orElse(null);

            if (product != null) {
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);
            } else {
                log.warn("Product ID {} not found while restoring stock for Order {}", item.getProductId(), order.getId());
            }
        });

        orderRepository.save(order);

        // 5. Log BUSINESS failure (Cancellation)
        log.warn("Order Cancelled: ID {} - Payment Failed or Abandoned", order.getId());
    }

    private boolean checkPaymentStatus(String paymentId) {
        return (int) (10 * Math.random()) == 1;
    }
}
