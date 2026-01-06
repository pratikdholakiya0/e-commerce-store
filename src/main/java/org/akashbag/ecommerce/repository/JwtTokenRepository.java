package org.akashbag.ecommerce.repository;

import org.akashbag.ecommerce.model.JwtToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JwtTokenRepository extends MongoRepository<JwtToken,String> {
    void deleteByToken(String token);

    boolean existsByToken(String token);

    void deleteJwtTokensByUsername(String username);
}
