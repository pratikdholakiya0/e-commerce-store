package org.akashbag.ecommerce.dto.response;

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
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private ProductCategory category;
    private List<String> images;
    private double price;
    private double discountedPrice;
    private int quantity;
    private double averageRating;
    private int totalRating;
    private boolean isLive;
}
