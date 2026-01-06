package org.akashbag.ecommerce.repository;

import org.akashbag.ecommerce.model.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends MongoRepository<Profile, String> {
    Profile findByUsername(String username);

    Profile findByUserId(String userId);

    boolean existsByUsername(String username);

    boolean existsByContactNo(String contactNo);
}
