package org.akashbag.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoice {
    @Id
    private String id;
    @Indexed(unique = true)
    private String orderId;
    private String customerId;
    private String invoiceUrl;
    @CreatedDate
    private LocalDate invoiceDate;
}
