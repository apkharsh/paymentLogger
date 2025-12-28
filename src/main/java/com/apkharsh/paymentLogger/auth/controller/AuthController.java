package com.apkharsh.paymentLogger.auth.controller;

import com.apkharsh.paymentLogger.auth.dto.*;
import com.apkharsh.paymentLogger.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @SneakyThrows
    @PostMapping(value = "/signup")
    public ResponseEntity<SignupResponse> signUp(@RequestBody SignupRequest request) {
        return new ResponseEntity<>(authService.signUp(request), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping(value = "/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        return new ResponseEntity<>(authService.login(request, response), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                                 HttpServletResponse response) {
        return new ResponseEntity<>(authService.refreshAccessToken(refreshToken, response), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        return new ResponseEntity<>(authService.logout(response), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping("/password-reset/request")
    public ResponseEntity<String> forgetPasswordOtpSend(@RequestBody PasswordResetRequest request) {
        return new ResponseEntity<>(authService.forgetPasswordOtpSend(request), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping("/password-reset/verify")
    public ResponseEntity<String> forgetPasswordOtpVerify(@RequestBody PasswordResetRequest request) {
        return new ResponseEntity<>(authService.forgetPasswordOtpVerify(request), HttpStatus.OK);
    }

    @SneakyThrows
    @GetMapping("/password-reset/confirm")
    public ResponseEntity<String> updateLoginPassword(@RequestBody PasswordResetRequest request) {
        return new ResponseEntity<>(authService.updateLoginPassword(request), HttpStatus.OK);
    }

}
