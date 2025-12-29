package com.apkharsh.paymentLogger.email;

public interface EmailService {

    void sendOTPEmail(String toEmail, String otp);

    void sendPasswordChangeConfirmation(String toEmail, String userName);

    void sendSecurityAlert(String toEmail, String userName);

    void sendOTPEmailAsync(String toEmail, String otp);

    void sendPasswordChangeConfirmationAsync(String toEmail, String userName);

    void sendSecurityAlertAsync(String toEmail, String userName);

    void sendWelcomeEmail(String toEmail, String userName);
}
