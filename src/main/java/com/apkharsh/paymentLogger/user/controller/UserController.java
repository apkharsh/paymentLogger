package com.apkharsh.paymentLogger.user.controller;

import com.apkharsh.paymentLogger.user.dto.PayerEnrollRequest;
import com.apkharsh.paymentLogger.user.dto.PayerEnrollResponse;
import com.apkharsh.paymentLogger.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // add payments -> for that need add payee controller
    @PostMapping
    public ResponseEntity<PayerEnrollResponse> signup(@RequestBody PayerEnrollRequest userEnrollRequest) {
        return new ResponseEntity<>(userService.payerEnroll(userEnrollRequest), HttpStatus.OK);
    }
}
