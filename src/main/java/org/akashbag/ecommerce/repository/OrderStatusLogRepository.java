package org.akashbag.ecommerce.repository;

import org.akashbag.ecommerce.model.OrderStatusLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusLogRepository extends MongoRepository<OrderStatusLog,String> {
    List<OrderStatusLog> findAllByOrderId(String orderId);
}
