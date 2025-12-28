package com.apkharsh.paymentLogger.auth.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
    private int otp;
    private String newPassword;
}