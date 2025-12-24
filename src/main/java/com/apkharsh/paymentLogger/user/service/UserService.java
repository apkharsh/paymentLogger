package com.apkharsh.paymentLogger.user.service;

import com.apkharsh.paymentLogger.user.dto.PayerEnrollRequest;
import com.apkharsh.paymentLogger.user.dto.PayerEnrollResponse;

public interface UserService {
    PayerEnrollResponse payerEnroll(PayerEnrollRequest userEnrollRequest);
}
