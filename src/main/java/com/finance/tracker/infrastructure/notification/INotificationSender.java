package com.finance.tracker.infrastructure.notification;

public interface INotificationSender {
    void send(String message, String recipientId);
}
