package org.akashbag.ecommerce.repository;

import org.akashbag.ecommerce.enums.OrderStatus;
import org.akashbag.ecommerce.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    List<Order> findAllByOrderStatusAndOrderDateBefore(OrderStatus orderStatus, LocalDateTime orderDateBefore);

    List<Order> findByUserIdOrderByOrderDateDesc(String id);

    List<Order> findAllByUserId(String userId);
}
