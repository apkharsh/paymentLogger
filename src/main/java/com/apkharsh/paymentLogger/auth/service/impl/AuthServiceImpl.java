package com.apkharsh.paymentLogger.auth.service.impl;

import com.apkharsh.paymentLogger.auth.enums.ROLE;
import com.apkharsh.paymentLogger.auth.dto.LoginRequest;
import com.apkharsh.paymentLogger.auth.dto.TokenResponse;
import com.apkharsh.paymentLogger.auth.dto.SignupRequest;
import com.apkharsh.paymentLogger.auth.dto.SignupResponse;
import com.apkharsh.paymentLogger.auth.service.AuthService;
import com.apkharsh.paymentLogger.exceptions.NotFoundException;
import com.apkharsh.paymentLogger.exceptions.ValidationException;
import com.apkharsh.paymentLogger.security.JwtService;
import com.apkharsh.paymentLogger.user.entity.User;
import com.apkharsh.paymentLogger.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static com.apkharsh.paymentLogger.security.util.JwtUtil.buildJWTClaims;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public SignupResponse signUp(SignupRequest request) throws Exception {

        if(userRepository.existsByEmail(request.getEmail())){
            throw new ValidationException("User Already exists with this Email id: " + request.getEmail());
        }
        User user = createUserFromRequest(request);

        userRepository.save(user);
        return buildSignupResponse(user);
    }

    public TokenResponse login(LoginRequest request,
                               HttpServletResponse response) throws Exception {

        // 1️⃣ Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Account does not exists with this email: " + request.getEmail()));

        // 2️⃣ Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ValidationException("Invalid password");
        }

        // 3️⃣ Build claims if you need them
        Map<String, Object> claims = buildJWTClaims(user);

        // 4️⃣ Generate tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getId()); // ⬅️ new method

        // 5️⃣ Set secure HttpOnly cookie for refresh token
        ResponseCookie cookie = ResponseCookie.from("refresh", refreshToken)
                .httpOnly(true)              // ⛔ JS cannot read it
                .secure(true)                // ⛔ HTTPS only
                .sameSite("None")          // ⛔ cross-site allowed for demo purposes
                .path("/auth/refresh")       // ⛔ only sent to refresh endpoint
                .maxAge(Duration.ofDays(14)) // refresh expiry
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // 6️⃣ Return access token only
        return new TokenResponse(accessToken);
    }

    @Override
    public TokenResponse refreshAccessToken(String refreshToken) throws Exception {
        String userId = jwtService.validateRefreshAndGetSubject(refreshToken);
        String newAccessToken = jwtService.generateAccessToken(userId, Map.of());
        return new TokenResponse(newAccessToken);
    }

    @Override
    public String logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refresh", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth/refresh")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());
        return "Logged out successfully";
    }

    private User createUserFromRequest(SignupRequest request) {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(ROLE.USER) // NTD LATER
                .name(request.getName()).build();
    }

    private SignupResponse buildSignupResponse(User user) {
        SignupResponse signupResponse = new SignupResponse();
        signupResponse.setUserID(UUID.fromString(user.getId()));
        signupResponse.setEmail(user.getEmail());
        signupResponse.setName(user.getName());
        return signupResponse;
    }

}
