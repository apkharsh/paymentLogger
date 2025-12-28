package com.apkharsh.paymentLogger.email;

public interface EmailService {

    void sendOTPEmail(String toEmail, String otp);

    void sendPasswordChangeConfirmation(String toEmail, String userName);
}
