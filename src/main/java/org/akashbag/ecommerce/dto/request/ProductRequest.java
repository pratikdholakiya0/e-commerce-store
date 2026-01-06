package org.akashbag.ecommerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.akashbag.ecommerce.enums.ProductCategory;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {
    private String name;
    private String description;
    private ProductCategory category;
    private double price;
    private List<String> images;
    private double discountedPrice;
    private int quantity;
    private boolean isLive;
}
