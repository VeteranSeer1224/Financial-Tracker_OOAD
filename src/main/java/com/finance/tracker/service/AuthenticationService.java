package com.finance.tracker.service;

import com.finance.tracker.model.entity.User;
import com.finance.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    @Autowired
    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user in the system.
     * @param user The user object containing registration details.
     * @return The saved User object.
     */
    public User register(User user) {
        // Check if user already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists.");
        }
        
        // In a real application, hash the password here before saving
        return userRepository.save(user);
    }

    /**
     * Authenticates a user based on email and password.
     * @param email The user's email.
     * @param password The user's password.
     * @return true if credentials match, false otherwise.
     */
    public boolean login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // In a real application, use a PasswordEncoder to match hashes
            return user.getPassword().equals(password);
        }
        
        return false;
    }

    /**
     * Logs out the user. 
     * In a stateless REST API (using JWTs), this might involve adding the token to a blacklist.
     * For session-based authentication, this would invalidate the session.
     * @param userId The ID of the user logging out.
     */
    public void logout(String userId) {
        // Implementation depends on the session management strategy (JWT vs Session)
        // For now, this is a placeholder to match the architectural design.
        System.out.println("User " + userId + " logged out successfully.");
    }
}
