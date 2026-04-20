package com.finance.tracker.controller;

import com.finance.tracker.model.entity.Notification;
import com.finance.tracker.service.NotificationScheduler;
import com.finance.tracker.service.NotificationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class NotificationController {

    private final NotificationScheduler notificationScheduler;
    private final NotificationService notificationService;

    @PostMapping("/notifications/scheduler/run")
    public Map<String, String> runSchedulerNow() {
        notificationScheduler.runScheduledCheck();
        return Map.of("status", "Scheduler run completed");
    }

    @GetMapping("/users/{userId}/notifications")
    public List<Notification> getUserNotifications(@PathVariable UUID userId) {
        return notificationService.getUserNotifications(userId);
    }

    @PatchMapping("/users/{userId}/notifications/{notificationId}/read")
    public Notification markAsRead(@PathVariable UUID userId, @PathVariable UUID notificationId) {
        return notificationService.markAsRead(userId, notificationId);
    }
}