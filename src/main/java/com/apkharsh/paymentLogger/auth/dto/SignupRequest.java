package com.apkharsh.paymentLogger.auth.dto;

import lombok.Data;


@Data
public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private int otp;
}
