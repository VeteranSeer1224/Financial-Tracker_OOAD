package com.finance.tracker.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.finance.tracker.model.payment.PaymentMethod;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    private String name;
    private String email;
    private String currencySetting;

    @ElementCollection
    @CollectionTable(name = "user_notification_preferences", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "preference_key")
    @Column(name = "enabled")
    @Builder.Default
    private Map<String, Boolean> notificationPreferences = new HashMap<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Budget> budgets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    public void updateProfile(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void setNotificationPreferences(Map<String, Boolean> prefs) {
        if (this.notificationPreferences == null) {
            this.notificationPreferences = new HashMap<>();
        } else {
            this.notificationPreferences.clear();
        }
        if (prefs != null) {
            this.notificationPreferences.putAll(prefs);
        }
    }
}
