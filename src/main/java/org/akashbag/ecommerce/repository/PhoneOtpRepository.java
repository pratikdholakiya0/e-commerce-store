package org.akashbag.ecommerce.repository;

import org.akashbag.ecommerce.model.PhoneOtp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneOtpRepository extends MongoRepository<PhoneOtp,String> {
    void deleteByUserId(String userId);

    PhoneOtp findByUserId(String userId);
}
