package com.apkharsh.paymentLogger.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BeneficiaryEnrollRequest {
    @NotBlank(message = "Payee email is required")
    @Email(message = "Invalid email format")
    private String payeeEmail;

    @NotBlank(message = "Payee alias is required")
    @Size(min = 2, max = 50, message = "Alias must be between 2 and 50 characters")
    private String payeeAlias;
}