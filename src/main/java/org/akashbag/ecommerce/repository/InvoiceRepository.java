package org.akashbag.ecommerce.repository;

import org.akashbag.ecommerce.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    Invoice findByOrderId(String orderId);
}
