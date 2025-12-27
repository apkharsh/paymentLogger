package com.apkharsh.paymentLogger.user.controller;

import com.apkharsh.paymentLogger.beneficiery.entity.Beneficiary;
import com.apkharsh.paymentLogger.user.dto.BeneficiaryEnrollRequest;
import com.apkharsh.paymentLogger.user.dto.BeneficiaryEnrollResponse;
import com.apkharsh.paymentLogger.user.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/beneficiary")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService userService;

    // add payments -> for that need add payee controller
    @PostMapping(value = "/enroll")
    public ResponseEntity<BeneficiaryEnrollResponse> addBeneficiary(@Valid @RequestBody BeneficiaryEnrollRequest userEnrollRequest) {
        return new ResponseEntity<>(userService.beneficiaryEnroll(userEnrollRequest), HttpStatus.OK);
    }

    @GetMapping(value = "/payer")
    public ResponseEntity<List<Beneficiary>> getBeneficiariesByPayerEmail(@RequestParam String payerId) {
        return new ResponseEntity<>(userService.getBeneficiaries(payerId), HttpStatus.OK);
    }
}
