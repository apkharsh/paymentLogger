package com.apkharsh.paymentLogger.user.service.impl;

import com.apkharsh.paymentLogger.beneficiery.entity.Beneficiary;
import com.apkharsh.paymentLogger.beneficiery.repository.BeneficiaryRepository;
import com.apkharsh.paymentLogger.exceptions.ValidationException;
import com.apkharsh.paymentLogger.user.dto.BeneficiaryEnrollRequest;
import com.apkharsh.paymentLogger.user.dto.BeneficiaryEnrollResponse;
import com.apkharsh.paymentLogger.user.entity.User;
import com.apkharsh.paymentLogger.user.repository.UserRepository;
import com.apkharsh.paymentLogger.user.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.apkharsh.paymentLogger.security.util.SecurityUtils.getCurrentUserId;

@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    @Override
    public BeneficiaryEnrollResponse beneficiaryEnroll(BeneficiaryEnrollRequest request) {
        String payerId = getCurrentUserId();

        // payee should exist in the system only then payer can add someone as beneficiary
        Optional<User> userOptional = userRepository.findByEmail(request.getPayeeEmail());
        if (userOptional.isEmpty()) {
            throw new ValidationException("User not found");
        }

        String payeeId = userOptional.get().getId();
        // add this user in beneficiary table
        boolean payeeAlreadyExists = beneficiaryRepository
                .existsByPayerIdAndPayeeId(payerId, payeeId);

        if (payeeAlreadyExists) {
            throw new ValidationException("Beneficiary already exists");
        }

        Beneficiary beneficiary = Beneficiary.builder()
                .id(UUID.randomUUID().toString())
                .payerId(payerId)
                .payeeId(payeeId)
                .payeeAlias(request.getPayeeAlias())
                .build();

        beneficiaryRepository.save(beneficiary);

        return buildPayerEnrollSuccessResponse(request);
    }

    @Override
    public List<Beneficiary> getBeneficiaries(String payerId) {
        return beneficiaryRepository.findByPayerId(payerId).orElseGet(List::of);
    }

    private BeneficiaryEnrollResponse buildPayerEnrollSuccessResponse(BeneficiaryEnrollRequest request) {
        return BeneficiaryEnrollResponse.builder().beneficiaryName("DONE").build();
    }
}
