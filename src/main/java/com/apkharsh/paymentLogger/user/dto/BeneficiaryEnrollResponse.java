package com.apkharsh.paymentLogger.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BeneficiaryEnrollResponse {
    private String payerEmail;
    private String beneficiaryName;
    private String beneficiaryEmail;
}
