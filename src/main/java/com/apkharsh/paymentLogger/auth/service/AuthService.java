package com.apkharsh.paymentLogger.auth.service;

import com.apkharsh.paymentLogger.auth.entity.User;
import com.apkharsh.paymentLogger.auth.validator.AuthValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthValidator authValidator;

    public String Signup(User request) {
        authValidator.validateSignupRequest(request);

        // validate Request
        return "";
    }
}
