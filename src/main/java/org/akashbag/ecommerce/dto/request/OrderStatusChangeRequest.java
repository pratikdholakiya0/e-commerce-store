package org.akashbag.ecommerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.akashbag.ecommerce.enums.OrderStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatusChangeRequest {
    private String message;
    private OrderStatus status;
    private String arrivedAt;
    private LocalDateTime expectedDeliveryDate;
}
