package com.apkharsh.paymentLogger.auth.controller;

import com.apkharsh.paymentLogger.auth.dto.LoginRequest;
import com.apkharsh.paymentLogger.auth.dto.LoginResponse;
import com.apkharsh.paymentLogger.auth.dto.SignupRequest;
import com.apkharsh.paymentLogger.auth.dto.SignupResponse;
import com.apkharsh.paymentLogger.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @SneakyThrows
    @PostMapping(value = "/signup")
    public ResponseEntity<SignupResponse> signUp(@RequestBody  SignupRequest request) {
        return new ResponseEntity<>(authService.signUp(request), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping(value = "/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
    }
}
