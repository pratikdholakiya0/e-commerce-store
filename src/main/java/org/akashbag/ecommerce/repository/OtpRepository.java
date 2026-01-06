package org.akashbag.ecommerce.repository;

import org.akashbag.ecommerce.model.Otp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends MongoRepository<Otp, String> {
    Otp getOtpById(String id);

    Otp getOtpByEmail(String email);
}
