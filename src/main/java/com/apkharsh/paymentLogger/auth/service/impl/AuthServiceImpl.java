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
                .orElseThrow(() -> new NotFoundException("Account does not exist with this email: " + request.getEmail()));

        // 2️⃣ Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ValidationException("Invalid password");
        }

        // 3️⃣ Build claims
        Map<String, Object> claims = buildJWTClaims(user);

        // 4️⃣ Generate tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        // ⭐ Debug logs
        System.out.println("Generated Access Token: " + accessToken.substring(0, 20) + "...");
        System.out.println("Generated Refresh Token: " + refreshToken.substring(0, 20) + "...");

        // 5️⃣ Set access token cookie
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false)  // ⚠️ MUST be false for http://localhost
                .sameSite("Lax")  // ⚠️ Changed from None to Lax (works for same-site)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());

        // 6️⃣ Set refresh token cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)  // ⚠️ MUST be false for http://localhost
                .sameSite("Lax")  // ⚠️ Changed from Strict to Lax
                .path("/")  // ⚠️ Changed from /auth/refresh to / (sent with all requests)
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        // ⭐ Debug: Print what cookies were set
        System.out.println("Set-Cookie: " + accessCookie.toString());
        System.out.println("Set-Cookie: " + refreshCookie.toString());

        // 7️⃣ Return user info (NOT tokens in response body for better security)
        return TokenResponse.builder()
                .user(user)
                .build();
    }

    @Override
    public TokenResponse refreshAccessToken(String refreshToken, HttpServletResponse response) {
        // 1️⃣ Validate refresh token
        if (refreshToken == null) {
            throw new ValidationException("Refresh token is missing");
        }

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new ValidationException("Invalid or expired refresh token");
        }

        // 2️⃣ Extract user ID and get user
        String userId = jwtService.extractSubject(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // 3️⃣ Generate new access token
        Map<String, Object> claims = buildJWTClaims(user);
        String newAccessToken = jwtService.generateAccessToken(userId, claims);

        // 4️⃣ Optional: Generate new refresh token (Token Rotation - RECOMMENDED)
        // String newRefreshToken = jwtService.generateRefreshToken(userId);

        // 5️⃣ Set new access token cookie
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());

        return TokenResponse.builder().user(user)
                .build();
    }

    @Override
    public String logout(HttpServletResponse response) {
        // Clear both cookies by setting maxAge to 0
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)  // ⚠️ false for localhost
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)  // ⚠️ false for localhost
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
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
