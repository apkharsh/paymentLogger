package com.apkharsh.paymentLogger.beneficiery.repository;

import com.apkharsh.paymentLogger.beneficiery.entity.Beneficiary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface BeneficiaryRepository extends MongoRepository<Beneficiary, String> {
    Optional<List<Beneficiary>> getBeneficiariesByPayerId(String Id);
    boolean existsByPayerIdAndPayeeId(String payerId, String payeeId);
}
