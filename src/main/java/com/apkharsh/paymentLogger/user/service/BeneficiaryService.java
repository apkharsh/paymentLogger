package com.apkharsh.paymentLogger.user.service;

import com.apkharsh.paymentLogger.beneficiery.entity.Beneficiary;
import com.apkharsh.paymentLogger.user.dto.BeneficiaryEnrollRequest;
import com.apkharsh.paymentLogger.user.dto.BeneficiaryEnrollResponse;

import java.util.List;

public interface BeneficiaryService {

    BeneficiaryEnrollResponse beneficiaryEnroll(BeneficiaryEnrollRequest userEnrollRequest);

    List<Beneficiary> getBeneficiaries(String payerId);
}
