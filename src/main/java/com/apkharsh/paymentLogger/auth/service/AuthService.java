package com.apkharsh.paymentLogger.auth.service;

import com.apkharsh.paymentLogger.auth.dto.*;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    SignupResponse signUp(SignupRequest request) throws Exception;

    TokenResponse login(LoginRequest request, HttpServletResponse response) throws Exception;

    TokenResponse refreshAccessToken(String refreshToken, HttpServletResponse response) throws Exception;

    String logout(HttpServletResponse response) throws Exception;

    String forgetPasswordOtpSend(PasswordResetRequest request) throws Exception;

    String forgetPasswordOtpVerify(PasswordResetRequest request) throws Exception;

    String updateLoginPassword(PasswordResetRequest request) throws Exception;

}
