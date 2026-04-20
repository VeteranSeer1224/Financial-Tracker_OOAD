package com.finance.tracker.controller;

import com.finance.tracker.dto.auth.LoginRequest;
import com.finance.tracker.dto.user.CreateUserRequest;
import com.finance.tracker.exception.ValidationException;
import com.finance.tracker.model.entity.User;
import com.finance.tracker.service.AuthenticationService;
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

    @PostMapping("/register")
    public User register(@Valid @RequestBody CreateUserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .currencySetting(request.getCurrencySetting())
                .notificationPreferences(request.getNotificationPreferences())
                .build();
        return authenticationService.register(user);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        boolean authenticated = authenticationService.login(request.getEmail(), request.getPassword());
        if (!authenticated) {
            throw new ValidationException("Invalid email or password");
        }
        return Map.of("authenticated", true);
    }

    @PostMapping("/logout/{userId}")
    public Map<String, Object> logout(@PathVariable UUID userId) {
        authenticationService.logout(userId);
        return Map.of("loggedOut", true, "userId", userId);
    }
}
