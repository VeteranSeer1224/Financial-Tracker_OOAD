package com.finance.tracker.controller;

import com.finance.tracker.dto.auth.InitiateOtpRequest;
import com.finance.tracker.dto.auth.LoginRequest;
import com.finance.tracker.dto.auth.OtpRequest;
import com.finance.tracker.dto.user.CreateUserRequest;
import com.finance.tracker.exception.ValidationException;
import com.finance.tracker.model.entity.User;
import com.finance.tracker.service.AuthenticationService;
import com.finance.tracker.service.OtpService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final OtpService otpService;

    @PostMapping("/otp/request")
    public Map<String, Object> requestOtp(@Valid @RequestBody InitiateOtpRequest request) {
        otpService.generateOtp(request.getEmail());
        return Map.of("message", "OTP generated. Check server console.");
    }

    @PostMapping("/otp/verify")
    public Map<String, Object> verifyOtp(@Valid @RequestBody OtpRequest request) {
        boolean valid = otpService.validateOtp(request.getEmail(), request.getOtp());
        return Map.of("valid", valid);
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody CreateUserRequest request) {
        if (!otpService.validateOtp(request.getEmail(), request.getOtp())) {
            throw new ValidationException("Invalid or expired OTP");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .currencySetting(request.getCurrencySetting())
                .password(request.getPassword())
                .notificationPreferences(request.getNotificationPreferences())
                .build();
        return authenticationService.register(user, request.getPassword());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        boolean authenticated = authenticationService.login(request.getEmail(), request.getPassword());
        if (!authenticated) {
            throw new ValidationException("Invalid email or password");
        }
        otpService.generateOtp(request.getEmail());
        return Map.of("message", "Credentials valid. OTP sent. Check server console.");
    }

    @PostMapping("/login/verify")
    public Map<String, Object> verifyLoginOtp(@Valid @RequestBody OtpRequest request) {
        boolean valid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            throw new ValidationException("Invalid or expired OTP");
        }
        User user = authenticationService.getUserByEmail(request.getEmail());
        return Map.of("authenticated", true, "userId", user.getUserId());
    }

    @PostMapping("/logout/{userId}")
    public Map<String, Object> logout(@PathVariable UUID userId) {
        authenticationService.logout(userId);
        return Map.of("loggedOut", true, "userId", userId);
    }
}
