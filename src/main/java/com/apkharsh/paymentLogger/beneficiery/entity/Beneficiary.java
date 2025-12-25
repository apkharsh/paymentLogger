package com.apkharsh.paymentLogger.beneficiery.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "Beneficiaries")
@Builder
public class Beneficiary {
    @Id
    String id;
    String payerId;
    String payeeId;
    String payeeAlias;
}
