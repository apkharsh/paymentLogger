package com.apkharsh.paymentLogger.ledger.dto;

import com.apkharsh.paymentLogger.user.dto.UserInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LedgerResponse {

    private String id;
    private String payerId;
    private String payeeId;
    private BigDecimal amount;

    /**
     * Payment timestamp in ISO-8601 format (UTC)
     * Frontend will convert to user's local timezone for display
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant timestamp;

    private String description;
    private String category;

    /**
     * User details populated via MongoDB aggregation
     */
    private UserInfo payer;
    private UserInfo payee;

    /**
     * Audit timestamps
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant updatedAt;

}
