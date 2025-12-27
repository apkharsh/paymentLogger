package com.apkharsh.paymentLogger.auth.dto;

import com.apkharsh.paymentLogger.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TokenResponse {
    private User user;
}
