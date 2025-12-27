package com.apkharsh.paymentLogger.ledger.service;

import com.apkharsh.paymentLogger.ledger.dto.LedgerRequest;
import com.apkharsh.paymentLogger.ledger.dto.LedgerResponse;
import com.apkharsh.paymentLogger.ledger.dto.LedgerSearchRequest;

import java.util.List;

public interface LedgerService {
    LedgerResponse addLedger(LedgerRequest ledgerRecord);
    List<LedgerResponse> getAllLedgers(LedgerSearchRequest request);
}
