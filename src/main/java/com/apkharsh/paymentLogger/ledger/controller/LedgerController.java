package com.apkharsh.paymentLogger.ledger.controller;


import com.apkharsh.paymentLogger.ledger.dto.LedgerRequest;
import com.apkharsh.paymentLogger.ledger.dto.LedgerResponse;
import com.apkharsh.paymentLogger.ledger.dto.LedgerSearchRequest;
import com.apkharsh.paymentLogger.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/ledgers")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping
    public ResponseEntity<LedgerResponse> createLedger(@RequestBody LedgerRequest request) {
        return new ResponseEntity<>(ledgerService.addLedger(request), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<LedgerResponse>> getAllLedgers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return new ResponseEntity<>(ledgerService.getAllLedgers(LedgerSearchRequest.builder().startDate(startDate).endDate(endDate).build()), HttpStatus.OK);
    }

}
