package com.finance.tracker.controller;

import com.finance.tracker.model.entity.Notification;
import com.finance.tracker.repository.NotificationRepository;
import com.finance.tracker.service.NotificationScheduler;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class NotificationController {
    private final NotificationScheduler notificationScheduler;
    private final NotificationRepository notificationRepository;

    @PostMapping("/notifications/scheduler/run")
    public String runSchedulerNow() {
        notificationScheduler.runScheduledCheck();
        return "Scheduler run completed";
    }

    @GetMapping("/users/{userId}/notifications")
    public List<Notification> getUserNotifications(@PathVariable UUID userId) {
        return notificationRepository.findByUserUserId(userId);
    }
}
