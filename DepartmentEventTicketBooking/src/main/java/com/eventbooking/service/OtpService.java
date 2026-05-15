package com.eventbooking.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final SecureRandom random = new SecureRandom();

    // key -> OtpEntry
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    public String generateOtp(String key) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        otpStore.put(key, new OtpEntry(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
        return otp;
    }

    public boolean validateOtp(String key, String otp) {
        OtpEntry entry = otpStore.get(key);
        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expiry())) {
            otpStore.remove(key);
            return false;
        }
        if (entry.otp().equals(otp)) {
            otpStore.remove(key);
            return true;
        }
        return false;
    }

    public boolean hasOtp(String key) {
        OtpEntry entry = otpStore.get(key);
        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expiry())) {
            otpStore.remove(key);
            return false;
        }
        return true;
    }

    private record OtpEntry(String otp, LocalDateTime expiry) {}
}
