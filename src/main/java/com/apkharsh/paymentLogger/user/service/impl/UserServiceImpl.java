package com.apkharsh.paymentLogger.user.service.impl;

import com.apkharsh.paymentLogger.beneficiery.entity.Beneficiary;
import com.apkharsh.paymentLogger.beneficiery.repository.BeneficiaryRepository;
import com.apkharsh.paymentLogger.user.dto.PayerEnrollRequest;
import com.apkharsh.paymentLogger.user.dto.PayerEnrollResponse;
import com.apkharsh.paymentLogger.user.entity.User;
import com.apkharsh.paymentLogger.user.repository.UserRepository;
import com.apkharsh.paymentLogger.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    @Override
    public PayerEnrollResponse payerEnroll(PayerEnrollRequest request) {
        // payee should exist in the system only then payer can add someone as beneficiary
        Optional<User> userOptional = userRepository.findByEmail(request.getBeneficiaryEmail());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // add this payer in current user's beneficiary
        Optional<Beneficiary> beneficiaryOptional = beneficiaryRepository.findByPayerId(request.getPayerEmail());
        if (beneficiaryOptional.isPresent()) {
            Beneficiary beneficiary = beneficiaryOptional.get();
            List<String> existingBeneficiaries = beneficiary.getBeneficiaries();
            if(existingBeneficiaries.contains(request.getPayerEmail())){
                throw new RuntimeException("Beneficiary already exists");
            }
            existingBeneficiaries.add(request.getBeneficiaryEmail());
            beneficiaryRepository.save(beneficiary);
        } else {
            Beneficiary beneficiary = Beneficiary.builder()
                    .id(UUID.randomUUID().toString())
                    .beneficiaryEmail(request.getPayerEmail())
                    .beneficiaries(List.of(request.getBeneficiaryEmail())).build();
            beneficiaryRepository.save(beneficiary);
        }
        return buildPayerEnrollSuccessResponse(request);
    }

    private PayerEnrollResponse buildPayerEnrollSuccessResponse(PayerEnrollRequest request) {
        return PayerEnrollResponse.builder()
                .payerEmail(request.getPayerEmail())
                .beneficiaryName(request.getBeneficiaryName())
                .beneficiaryEmail(request.getBeneficiaryEmail()).build();
    }
}
