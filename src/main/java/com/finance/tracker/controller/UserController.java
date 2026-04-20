package com.finance.tracker.controller;

import com.finance.tracker.dto.user.CreateUserRequest;
import com.finance.tracker.dto.user.UpdateUserRequest;
import com.finance.tracker.model.entity.User;
import com.finance.tracker.service.AuthenticationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final AuthenticationService authenticationService;

    @PostMapping
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .currencySetting(request.getCurrencySetting())
                .notificationPreferences(request.getNotificationPreferences())
                .build();
        return authenticationService.register(user);
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable UUID userId) {
        return authenticationService.getUser(userId);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return authenticationService.getAllUsers();
    }

    @PutMapping("/{userId}")
    public User updateUser(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        return authenticationService.updateProfile(userId, request.getName(), request.getEmail());
    }
}
