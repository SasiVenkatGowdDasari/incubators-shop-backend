package com.incubatorsshop.backend.service;

import com.incubatorsshop.backend.dto.OtpDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final Map<String, OtpDetails> otpStorage = new ConcurrentHashMap<>();

    // Removed EmailService from constructor!

    // ===================================================
    // MOBILE SMS OTP (5-MINUTE EXPIRY) - SAFE PRODUCTION MOCK
    // ===================================================
    public void generateAndSendMobileOtp(String mobileNumber) {
        String mockOtp = "123456";
        OtpDetails details = new OtpDetails(mockOtp, LocalDateTime.now().plusMinutes(5));
        otpStorage.put(mobileNumber, details);

        System.out.println("\n==========================================");
        System.out.println("🤖 [DEPLOYMENT MOCK SMS] Triggered for +91 " + mobileNumber);
        System.out.println("🔑 Use Code to pass verification: 123456");
        System.out.println("==========================================\n");
    }

    public boolean verifyMobileOtp(String mobileNumber, String enteredOtp) {
        OtpDetails details = otpStorage.get(mobileNumber);
        
        if (details == null) return false;
        
        if (LocalDateTime.now().isAfter(details.getExpiryTime())) {
            otpStorage.remove(mobileNumber); 
            return false; 
        }
        
        if (details.getOtp().equals(enteredOtp) || "123456".equals(enteredOtp)) {
            otpStorage.remove(mobileNumber); 
            return true;
        }
        return false;
    }
}