package com.finance.tracker.service;

import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.model.entity.User;
import com.finance.tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;

    @Transactional
    public User register(User user) {
        return register(user, user.getPassword());
    }

    @Transactional
    public User register(User user, String password) {
        user.setPassword(password);
        if (user.getNotificationPreferences() == null || user.getNotificationPreferences().isEmpty()) {
            Map<String, Boolean> defaults = new HashMap<>();
            defaults.put("RENEWAL_REMINDER", true);
            defaults.put("BUDGET_WARNING", true);
            defaults.put("BUDGET_EXCEEDED", true);
            user.setNotificationPreferences(defaults);
        }
        return userRepository.save(user);
    }

    public User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateProfile(UUID userId, String name, String email) {
        User user = getUser(userId);
        user.updateProfile(name, email);
        return userRepository.save(user);
    }

    public boolean login(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> Objects.equals(user.getPassword(), password))
                .orElse(false);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
    }

    public void logout(UUID userId) {
        getUser(userId);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        getUser(userId);
        userRepository.deleteById(userId);
    }
}
