package com.apkharsh.paymentLogger.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@Setter
@RequiredArgsConstructor
public class UserPrincipal {

    private final String userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
}
