package com.finance.tracker.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationSender implements INotificationSender {
    @Override
    public void send(String message, String recipientId) {
        log.info("Sending notification to {}: {}", recipientId, message);
    }
}
