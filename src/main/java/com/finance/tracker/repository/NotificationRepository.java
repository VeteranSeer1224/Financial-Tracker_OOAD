package com.finance.tracker.repository;

import com.finance.tracker.model.entity.Notification;
import com.finance.tracker.model.enums.NotificationType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserUserId(UUID userId);

    boolean existsByUserUserIdAndTypeAndReferenceIdAndReadFalse(UUID userId, NotificationType type, String referenceId);
}
