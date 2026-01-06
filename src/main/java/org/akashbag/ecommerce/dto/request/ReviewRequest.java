package org.akashbag.ecommerce.dto.request;

import lombok.Data;

@Data
public class ReviewRequest {
    private int rating;
    private String comment;
}