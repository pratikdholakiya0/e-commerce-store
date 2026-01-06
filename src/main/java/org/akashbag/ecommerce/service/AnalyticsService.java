package org.akashbag.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.response.DashBoardResponse;
import org.akashbag.ecommerce.enums.OrderStatus;
import org.akashbag.ecommerce.enums.PaymentStatus;
import org.akashbag.ecommerce.model.Order;
import org.akashbag.ecommerce.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final OrderRepository orderRepository;

    public DashBoardResponse getAnalytics(){
        List<Order> totalOrders = orderRepository.findAll();

        long activeOrders = totalOrders.stream()
                .filter(order -> order.getOrderStatus()!=OrderStatus.DELIVERED &&
                                order.getOrderStatus()!=OrderStatus.CANCELLED)
                .count();

        long canceledOrders = totalOrders.stream()
                .filter(o->o.getOrderStatus()==OrderStatus.CANCELLED)
                .count();

        double grossRevenue = totalOrders.stream()
                .filter(order -> order.getPaymentStatus()==PaymentStatus.COMPLETED)
                .mapToDouble(Order::getTotalPrice)
                .sum();

        double totalRefundRemaining = totalOrders.stream()
                .filter(order ->
                        order.getPaymentStatus()==PaymentStatus.COMPLETED &&
                        (order.getOrderStatus()==OrderStatus.RETURNED ||
                        order.getOrderStatus()==OrderStatus.CANCELLED))
                .mapToDouble(o->o.getTotalPrice()-o.getCancellationPenalty())
                .sum();

        double netRevenue = grossRevenue - totalRefundRemaining;

        return DashBoardResponse.builder()
                .totalOrders(totalOrders.size())
                .activeOrders(activeOrders)
                .canceledOrders(canceledOrders)
                .totalRevenue(netRevenue)
                .totalActiveRefund(totalRefundRemaining)
                .build();
    }
}
