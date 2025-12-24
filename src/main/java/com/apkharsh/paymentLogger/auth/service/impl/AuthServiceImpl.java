package com.apkharsh.paymentLogger.auth.service.impl;

import com.apkharsh.paymentLogger.auth.ROLE;
import com.apkharsh.paymentLogger.auth.dto.LoginRequest;
import com.apkharsh.paymentLogger.auth.dto.LoginResponse;
import com.apkharsh.paymentLogger.auth.dto.SignupRequest;
import com.apkharsh.paymentLogger.auth.dto.SignupResponse;
import com.apkharsh.paymentLogger.auth.service.AuthService;
import com.apkharsh.paymentLogger.security.JwtService;
import com.apkharsh.paymentLogger.user.entity.User;
import com.apkharsh.paymentLogger.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
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
            throw new Exception("User Already exists with this Email id: " + request.getEmail());
        }
        User user = createUserFromRequest(request);

        userRepository.save(user);
        return buildSignupResponse(user);
    }

    public LoginResponse login(LoginRequest request) throws Exception {
        String userEmail = request.getEmail();
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if(userOptional.isEmpty()){
            throw new Exception("Account does not exists with this email: " + userEmail);
        }
        User user = userOptional.get();

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new Exception("Incorrect Password");
        }

        Map<String, Object> claims = buildJWTClaims(user);

        return new LoginResponse(jwtService.generateToken(user.getId(), claims));
    }

    private User createUserFromRequest(SignupRequest request) {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(ROLE.USER)
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
