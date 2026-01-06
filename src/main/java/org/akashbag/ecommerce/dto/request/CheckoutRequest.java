package org.akashbag.ecommerce.dto.request;

import lombok.Data;
import org.akashbag.ecommerce.model.Address;

import java.util.List;

@Data
public class CheckoutRequest {
    private Address shippingAddress;
    private String paymentId;
    private List<String> selectedProductIds; // ✅ The new field
}