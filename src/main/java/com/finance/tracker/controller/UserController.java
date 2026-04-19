package com.finance.tracker.controller;

import com.finance.tracker.model.entity.User;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(AuthenticationService authenticationService, UserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = authenticationService.register(user);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (authenticationService.login(email, password)) {
            // In a real application, return a JWT token here
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @PostMapping("/{userId}/logout")
    public ResponseEntity<String> logout(@PathVariable String userId) {
        authenticationService.logout(userId);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable String userId, @RequestBody Map<String, String> updates) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        user.updateProfile(updates.get("name"), updates.get("email"));
        userRepository.save(user);
        
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/preferences")
    public ResponseEntity<?> updateNotificationPreferences(
            @PathVariable String userId, 
            @RequestBody Map<String, Boolean> preferences) {
        
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        user.setNotificationPreferences(preferences);
        userRepository.save(user);
        
        return ResponseEntity.ok(user);
    }
}
