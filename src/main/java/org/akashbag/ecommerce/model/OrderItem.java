package org.akashbag.ecommerce.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
    @NotBlank
    private String productId;
    private String productName;
    private String image;
    @Min(1)
    @Max(10)
    private int quantity;
    private double unitPrice;
    private double totalPrice;
}
