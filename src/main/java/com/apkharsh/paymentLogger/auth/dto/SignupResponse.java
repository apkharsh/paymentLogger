package com.apkharsh.paymentLogger.auth.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SignupResponse {
    private UUID userID;
    private String name;
    private String email;
}
