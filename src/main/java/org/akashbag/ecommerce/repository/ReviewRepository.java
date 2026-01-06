package org.akashbag.ecommerce.repository;

import org.akashbag.ecommerce.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProductId(String productId);

    boolean existsByUserId(String userId);

    boolean existsByUserIdAndProductId(String userId, String productId);
}
