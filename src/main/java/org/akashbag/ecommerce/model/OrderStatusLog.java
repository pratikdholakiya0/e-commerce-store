package org.akashbag.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.akashbag.ecommerce.enums.OrderStatus;
import org.akashbag.ecommerce.enums.Role;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "order_logs")
public class OrderStatusLog {
    @Id
    private String id;
    @Indexed
    private String orderId;
    private String message;
    private Role role;
    private OrderStatus orderStatus;
    @CreatedDate
    private LocalDateTime timestamp;
}
