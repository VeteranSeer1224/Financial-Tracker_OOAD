package com.finance.tracker.infrastructure.notification;

import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements INotificationSender {

    @Override
    public boolean send(String recipient, String subject, String message) {
        // In a real app, you would wire up JavaMailSender here.
        // For now, we simulate sending the email.
        System.out.println("--- EMAIL SENT ---");
        System.out.println("To: " + recipient);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        System.out.println("------------------");
        return true; // Simulate success
    }
}