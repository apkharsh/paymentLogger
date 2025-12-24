package com.apkharsh.paymentLogger.user.dto;

import lombok.Data;

@Data
public class PayerEnrollRequest {
    private String payerEmail;
    private String beneficiaryName;
    private String beneficiaryEmail;
}
