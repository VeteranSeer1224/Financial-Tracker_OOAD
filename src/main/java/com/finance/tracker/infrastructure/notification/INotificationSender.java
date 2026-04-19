package com.finance.tracker.infrastructure.notification;

public interface INotificationSender {
    boolean send(String recipient, String subject, String message);
}