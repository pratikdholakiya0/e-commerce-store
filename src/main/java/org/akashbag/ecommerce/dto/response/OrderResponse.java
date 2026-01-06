package org.akashbag.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.akashbag.ecommerce.enums.OrderStatus;
import org.akashbag.ecommerce.enums.PaymentStatus;
import org.akashbag.ecommerce.model.Address;
import org.akashbag.ecommerce.model.OrderItem;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private List<OrderItem> items;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentId;
    private Address shippingAddress;
    private double cancellationPenalty;
    private double totalPrice;
    private LocalDateTime expectedDeliveryDate;
    private String message;
    private String arrivedAt;
    private LocalDateTime orderDate;
}
