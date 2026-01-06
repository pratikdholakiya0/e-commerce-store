package org.akashbag.ecommerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.akashbag.ecommerce.enums.OrderStatus;
import org.akashbag.ecommerce.enums.PaymentStatus;
import org.akashbag.ecommerce.model.Address;
import org.akashbag.ecommerce.model.OrderItem;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private List<OrderItem> items;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentId;
    private Address shippingAddress;
}
