package org.akashbag.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.akashbag.ecommerce.enums.ProductCategory;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    @Indexed
    private String userId;
    @Indexed
    private String name;
    private String description;
    private ProductCategory category;
    private List<String> images;
    private double price;
    private double discountedPrice;
    private int quantity;
    private boolean isLive;
    private double averageRating;
    private int totalReviews;
    @CreatedBy
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}