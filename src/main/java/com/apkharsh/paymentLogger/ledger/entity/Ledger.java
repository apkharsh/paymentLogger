package com.apkharsh.paymentLogger.ledger.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "ledgers")
@CompoundIndexes({
        @CompoundIndex(name = "payer_timestamp_idx", def = "{'payerId': 1, 'timestamp': -1}"),
        @CompoundIndex(name = "payee_timestamp_idx", def = "{'payeeId': 1, 'timestamp': -1}"),
        @CompoundIndex(name = "timestamp_idx", def = "{'timestamp': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ledger {

    @Id
    private String id;

    @Indexed
    private String payerId;

    @Indexed
    private String payeeId;

    @NonNull
    private BigDecimal amount;

    /**
     * Payment timestamp in UTC
     * Stored as Instant for timezone-independent storage
     */
    @Indexed
    @NonNull
    private Instant timestamp;

    private String description;

    /**
     * Record creation timestamp (audit field)
     */
    @CreatedDate
    private Instant createdAt;

    /**
     * Record last modification timestamp (audit field)
     */
    @LastModifiedDate
    private Instant updatedAt;
}
