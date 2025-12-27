package com.apkharsh.paymentLogger.ledger.service.impl;

import com.apkharsh.paymentLogger.ledger.dto.LedgerRequest;
import com.apkharsh.paymentLogger.ledger.dto.LedgerResponse;
import com.apkharsh.paymentLogger.ledger.entity.Ledger;
import com.apkharsh.paymentLogger.ledger.repository.LedgerRepository;
import com.apkharsh.paymentLogger.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.apkharsh.paymentLogger.security.util.SecurityUtils.getCurrentUserId;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final LedgerRepository ledgerRepository;

    @Override
    public LedgerResponse addLedger(LedgerRequest request) {
        // ⭐ Simple: Use provided timestamp or default to now
        Instant timestamp = request.getTimestamp() != null
                ? request.getTimestamp()
                : Instant.now();

        Ledger ledger = Ledger.builder()
                .id(UUID.randomUUID().toString())
                .payerId(request.getPayerId())
                .payeeId(request.getPayeeId())
                .amount(request.getAmount())
                .timestamp(timestamp)
                .description(request.getDescription())
                .build();
        ledgerRepository.save(ledger);

        return LedgerResponse.builder()
                .id(ledger.getId())
                .payerId(ledger.getPayerId())
                .payeeId(ledger.getPayeeId())
                .amount(ledger.getAmount())
                .timestamp(ledger.getTimestamp())
                .description(ledger.getDescription())
                .build();
    }

    @Override
    public List<LedgerResponse> getAllLedgers() {
        // Add this temporary method to your repository
        return ledgerRepository.findAllLedgersWithUsersByUserId(getCurrentUserId());
    }
}
