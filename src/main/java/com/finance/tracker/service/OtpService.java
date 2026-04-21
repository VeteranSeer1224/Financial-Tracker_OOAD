package com.finance.tracker.service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class OtpService {
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateOtp(String email) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpStore.put(email, otp);
        System.out.println("[OTP] Code for " + email + ": " + otp);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String storedOtp = otpStore.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStore.remove(email);
            return true;
        }
        return false;
    }

    public void clearOtp(String email) {
        otpStore.remove(email);
    }
}
