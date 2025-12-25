package com.apkharsh.paymentLogger.security.util;

import com.apkharsh.paymentLogger.user.entity.User;

import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    public static Map<String, Object> buildJWTClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("role", user.getRole());
        return claims;
    }
}
