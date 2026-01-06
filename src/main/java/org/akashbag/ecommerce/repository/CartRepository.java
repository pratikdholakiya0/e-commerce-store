package org.akashbag.ecommerce.repository;

import org.akashbag.ecommerce.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
    Cart getCartByUserId(String userId);

    Optional<Cart> findByUserId(String userId);
}
