package com.apkharsh.paymentLogger.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayerEnrollResponse {
    private String payerEmail;
    private String beneficiaryName;
    private String beneficiaryEmail;
}
