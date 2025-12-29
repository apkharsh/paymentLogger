package com.apkharsh.paymentLogger.auth.service.impl;

import com.apkharsh.paymentLogger.auth.dto.*;
import com.apkharsh.paymentLogger.auth.enums.ROLE;
import com.apkharsh.paymentLogger.auth.service.AuthService;
import com.apkharsh.paymentLogger.email.EmailService;
import com.apkharsh.paymentLogger.exceptions.NotFoundException;
import com.apkharsh.paymentLogger.exceptions.ValidationException;
import com.apkharsh.paymentLogger.security.JwtService;
import com.apkharsh.paymentLogger.user.entity.User;
import com.apkharsh.paymentLogger.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.apkharsh.paymentLogger.security.util.JwtUtil.buildJWTClaims;
import static com.apkharsh.paymentLogger.security.util.SecurityUtils.getCurrentUserId;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    private static final String ACC_CREATE_OTP_KEY_PREFIX = "acc_create:otp:";
    private static final String ACC_VERIFIED_KEY_PREFIX = "acc_create:verified:";
    private static final String ACC_CREATE_RATE_LIMIT_KEY_PREFIX = "acc_create:rate_limit:";

    private static final String OTP_KEY_PREFIX = "pwd_reset:otp:";
    private static final String VERIFIED_KEY_PREFIX = "pwd_reset:verified:";
    private static final String ATTEMPT_KEY_PREFIX = "pwd_reset:attempts:";
    private static final String RATE_LIMIT_KEY_PREFIX = "pwd_reset:rate_limit:";
    private static final String REQUEST_LIMIT_KEY_PREFIX = "pwd_reset:requests:";
    private static final int MAX_REQUESTS_PER_HOUR = 10;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int VERIFIED_TOKEN_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RATE_LIMIT_SECONDS = 10;

    @Override
    public SignupResponse signUp(SignupRequest request) {

        String otpVerifiedKey = ACC_VERIFIED_KEY_PREFIX + request.getEmail();
        String isVerified = redisTemplate.opsForValue().get(otpVerifiedKey);

        if (isVerified == null || !isVerified.equals("true")) {
            throw new ValidationException("OTP verification required. Please verify OTP first.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("User Already exists with this Email id: " + request.getEmail());
        }
        User user = createUserFromRequest(request);

        userRepository.save(user);
        return buildSignupResponse(user);
    }

    @Override
    public String signUpSendOtp(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("User Already exists with this Email id: " + request.getEmail());
        }

        String otp = generateSecureOTP();

        String otpKey = ACC_CREATE_OTP_KEY_PREFIX + request.getEmail();
        String rateLimitKey = ACC_CREATE_RATE_LIMIT_KEY_PREFIX + request.getEmail();

        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            throw new ValidationException("Please wait " + RATE_LIMIT_SECONDS + " seconds before requesting another OTP");
        }

        redisTemplate.opsForValue().set(otpKey, otp, RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(otpKey, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);

        emailService.sendOTPEmail(request.getEmail(), otp);

        return "OTP sent successfully to " + request.getEmail();
    }

    @Override
    public String signUpVerifyOtp(SignupRequest request) {
        String inputOtp = String.valueOf(request.getOtp());
        String otpKey = ACC_CREATE_OTP_KEY_PREFIX + request.getEmail();
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null || !storedOtp.equals(inputOtp)) {
            throw new ValidationException("Invalid or expired OTP. Please request a new one.");
        }

        String verifiedKey = ACC_VERIFIED_KEY_PREFIX + request.getEmail();
        redisTemplate.opsForValue().set(verifiedKey, "true", VERIFIED_TOKEN_EXPIRY_MINUTES, TimeUnit.MINUTES);

        redisTemplate.delete(otpKey);
        redisTemplate.delete(storedOtp);
        return "OTP verified successfully";
    }

    @Override
    public TokenResponse login(LoginRequest request, HttpServletResponse response) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Account does not exist with this email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ValidationException("Invalid password");
        }

        Map<String, Object> claims = buildJWTClaims(user);

        String accessToken = jwtService.generateAccessToken(user.getId(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getId());

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


        return TokenResponse.builder()
                .user(user)
                .build();
    }

    @Override
    public TokenResponse refreshAccessToken(String refreshToken, HttpServletResponse response) {
        // Validate refresh token
        if (refreshToken == null) {
            throw new ValidationException("Refresh token is missing");
        }

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new ValidationException("Invalid or expired refresh token");
        }

        // Extract user ID and get user
        String userId = jwtService.extractSubject(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        //⃣ Generate new access token
        Map<String, Object> claims = buildJWTClaims(user);
        String newAccessToken = jwtService.generateAccessToken(userId, claims);

        //⃣ Optional: Generate new refresh token (Token Rotation - RECOMMENDED)
        // String newRefreshToken = jwtService.generateRefreshToken(userId);

        // Set new access token cookie
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

    @Override
    public String forgetPasswordOtpSend(PasswordUpdateRequest request) {
        String email = request.getEmail();

        if (!userRepository.existsByEmail(email)) {
            throw new NotFoundException("No account found with email: " + email);
        }

        // Check hourly request limit (abuse prevention)
        String requestLimitKey = REQUEST_LIMIT_KEY_PREFIX + email;
        int requestCount = getAttemptCount(requestLimitKey);

        if (requestCount >= MAX_REQUESTS_PER_HOUR) {
            throw new ValidationException(
                    "Too many password reset attempts. Please try again after 1 hour."
            );
        }

        String rateLimitKey = RATE_LIMIT_KEY_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            throw new ValidationException("Please wait 60 seconds before requesting another OTP");
        }

        String otp = generateSecureOTP();

        String otpKey = OTP_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(otpKey, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);

        // Reset verification attempt counter (fresh 3 attempts per OTP)
        String attemptKey = ATTEMPT_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(attemptKey, "0", OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);

        // Set 60s rate limit
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        // Increment hourly request counter
        if (requestCount == 0) {
            redisTemplate.opsForValue().set(requestLimitKey, "1", 1, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(requestLimitKey);
        }

        // Send email
        emailService.sendOTPEmail(email, otp);

        return "OTP sent successfully to " + email;
    }

    @Override
    public String forgetPasswordOtpVerify(PasswordUpdateRequest request) {
        String email = request.getEmail();
        String inputOtp = String.valueOf(request.getOtp());

        String otpKey = OTP_KEY_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            throw new ValidationException("OTP expired or not found. Please request a new one.");
        }

        // Check verification attempts (separate from request limit)
        String attemptKey = ATTEMPT_KEY_PREFIX + email;
        Integer attempts = getAttemptCount(attemptKey);

        if (attempts >= MAX_ATTEMPTS) {
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptKey);
            throw new ValidationException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        if (!storedOtp.equals(inputOtp)) {
            // Increment verification attempt counter
            Long newAttempts = redisTemplate.opsForValue().increment(attemptKey);
            int remainingAttempts = MAX_ATTEMPTS - newAttempts.intValue();

            throw new ValidationException(
                    "Invalid OTP. " + remainingAttempts + " attempt(s) remaining."
            );
        }

        String verifiedKey = VERIFIED_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(verifiedKey, "true", VERIFIED_TOKEN_EXPIRY_MINUTES, TimeUnit.MINUTES);

        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptKey);

        return "OTP verified successfully";
    }

    @Override
    @Transactional
    public String updateLoginPassword(PasswordUpdateRequest request) {
        String email = request.getEmail();
        String newPassword = request.getNewPassword();

        if (newPassword == null || newPassword.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }

        String verifiedKey = VERIFIED_KEY_PREFIX + email;
        String isVerified = redisTemplate.opsForValue().get(verifiedKey);

        if (isVerified == null || !isVerified.equals("true")) {
            throw new ValidationException("OTP verification required. Please verify OTP first.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Clean up all keys including request limit (reset on successful password change)
        redisTemplate.delete(verifiedKey);
        redisTemplate.delete(OTP_KEY_PREFIX + email);
        redisTemplate.delete(ATTEMPT_KEY_PREFIX + email);
        redisTemplate.delete(REQUEST_LIMIT_KEY_PREFIX + email);

        emailService.sendPasswordChangeConfirmation(email, user.getName());

        return "Password updated successfully";
    }

    @Override
    public String updateLoginPasswordAuthenticated(PasswordUpdateRequest request) {
        String currentUser = getCurrentUserId();
        User user = userRepository.findById(currentUser)
                .orElseThrow(() -> new NotFoundException("User not found"));
        String currentEncodedPassword = user.getPassword();
        String newRawPassword = request.getNewPassword();
        passwordEncoder.matches(newRawPassword, currentEncodedPassword);

        if (newRawPassword == null || newRawPassword.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        } else {
            user.setPassword(passwordEncoder.encode(newRawPassword));
            userRepository.save(user);
            emailService.sendPasswordChangeConfirmation(user.getEmail(), user.getName());
            return "Password updated successfully";
        }
    }

    private String generateSecureOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private Integer getAttemptCount(String attemptKey) {
        String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
        return attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
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
