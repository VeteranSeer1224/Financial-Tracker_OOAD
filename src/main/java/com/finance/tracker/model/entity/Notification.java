package com.finance.tracker.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.finance.tracker.model.enums.NotificationType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
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
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID notificationId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private String referenceId;
    private boolean silentlyDismissed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    @PrePersist
    public void initTimestamp() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void markAsRead() {
        this.read = true;
    }
}
