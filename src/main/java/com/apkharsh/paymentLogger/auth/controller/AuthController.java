package com.apkharsh.paymentLogger.auth.controller;

import com.apkharsh.paymentLogger.auth.entity.User;
import com.apkharsh.paymentLogger.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    public ResponseEntity<String> signUp(User request) {
        return new ResponseEntity<>(authService.Signup(request), HttpStatus.OK);
    }

}

// Request Types -> GET, POST, PUT, PATCH, DELETE
