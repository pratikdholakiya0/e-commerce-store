package org.akashbag.ecommerce.repository;

import org.akashbag.ecommerce.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
    User getUserByEmail(String email);

    <T> ScopedValue<T> findByEmail(String email);
}
