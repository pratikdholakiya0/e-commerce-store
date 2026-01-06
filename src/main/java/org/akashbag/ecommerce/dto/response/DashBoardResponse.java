package org.akashbag.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashBoardResponse {
    private long totalOrders;
    private long activeOrders;
    private long canceledOrders;
    private double totalActiveRefund;
    private double totalRevenue;
}
