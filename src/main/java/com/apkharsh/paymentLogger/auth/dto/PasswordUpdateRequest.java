package com.apkharsh.paymentLogger.auth.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    private String email;
    private int otp;
    private String existingPassword;
    private String newPassword;
}