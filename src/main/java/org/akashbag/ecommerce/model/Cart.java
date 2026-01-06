package org.akashbag.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cart {
    @Id
    private String id;
    @Indexed(unique = true)
    private String userId;
    private List<CartItem> cartItems = new ArrayList<>();
    private double totalPrice;
}
