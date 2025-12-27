package com.apkharsh.paymentLogger.auth.service;

import com.apkharsh.paymentLogger.auth.dto.LoginRequest;
import com.apkharsh.paymentLogger.auth.dto.TokenResponse;
import com.apkharsh.paymentLogger.auth.dto.SignupRequest;
import com.apkharsh.paymentLogger.auth.dto.SignupResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    SignupResponse signUp(SignupRequest request) throws Exception;

    TokenResponse login(LoginRequest request, HttpServletResponse response) throws Exception;

    TokenResponse refreshAccessToken(String refreshToken, HttpServletResponse response) throws Exception;

    String logout(HttpServletResponse response) throws Exception;
}
