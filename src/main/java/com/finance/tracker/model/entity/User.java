package com.finance.tracker.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "users") // "user" is a reserved keyword in many SQL dialects like PostgreSQL/H2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Necessary for AuthenticationService login

    @Column(nullable = false)
    private String currencySetting = "USD";

    // Maps the Map<String, Boolean> to a secondary table automatically
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_notification_prefs", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "preference_type")
    @Column(name = "is_enabled")
    private Map<String, Boolean> notificationPreferences = new HashMap<>();

    // Explicit methods matching the Class Diagram
    public void updateProfile(String name, String email) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (email != null && !email.trim().isEmpty()) {
            this.email = email;
        }
    }

    public void setNotificationPreferences(Map<String, Boolean> prefs) {
        if (prefs != null) {
            this.notificationPreferences = prefs;
        }
    }
}
