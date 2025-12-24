package com.apkharsh.paymentLogger.auth.service;

import com.apkharsh.paymentLogger.auth.dto.LoginRequest;
import com.apkharsh.paymentLogger.auth.dto.LoginResponse;
import com.apkharsh.paymentLogger.auth.dto.SignupRequest;
import com.apkharsh.paymentLogger.auth.dto.SignupResponse;

public interface AuthService {

    SignupResponse signUp(SignupRequest request) throws Exception;

    LoginResponse login(LoginRequest request) throws Exception;
}
