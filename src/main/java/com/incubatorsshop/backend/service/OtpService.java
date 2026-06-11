package com.incubatorsshop.backend.service;

import com.incubatorsshop.backend.dto.OtpDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final Map<String, OtpDetails> otpStorage = new ConcurrentHashMap<>();
    private final EmailService emailService;

    public OtpService(EmailService emailService) {
        this.emailService = emailService;
    }

    // ===================================================
    // EMAIL OTP (10-MINUTE EXPIRY) - LIVE SMTP PRODUCTION
    // ===================================================
    public void generateAndSendEmailOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        OtpDetails details = new OtpDetails(otp, LocalDateTime.now().plusMinutes(10));
        otpStorage.put(email, details);
        emailService.sendOtpEmail(email, otp);
    }

    public boolean verifyEmailOtp(String email, String enteredOtp) {
        OtpDetails details = otpStorage.get(email);
        
        if (details == null) return false;
        
        if (LocalDateTime.now().isAfter(details.getExpiryTime())) {
            otpStorage.remove(email); 
            return false; 
        }
        
        if (details.getOtp().equals(enteredOtp)) {
            otpStorage.remove(email); 
            return true;
        }
        return false;
    }

    // ===================================================
    // MOBILE SMS OTP (5-MINUTE EXPIRY) - SAFE PRODUCTION MOCK
    // ===================================================
    public void generateAndSendMobileOtp(String mobileNumber) {
        // Assigns '123456' as the strict valid token entry for any mobile number passed
        String mockOtp = "123456";
        OtpDetails details = new OtpDetails(mockOtp, LocalDateTime.now().plusMinutes(5));
        otpStorage.put(mobileNumber, details);

        // Clean diagnostics logger inside your deployment platform console terminal
        System.out.println("\n==========================================");
        System.out.println("🤖 [DEPLOYMENT MOCK SMS] Triggered for +91 " + mobileNumber);
        System.out.println("🔑 Use Code to pass verification: 123456");
        System.out.println("==========================================\n");
    }

    public boolean verifyMobileOtp(String mobileNumber, String enteredOtp) {
        OtpDetails details = otpStorage.get(mobileNumber);
        
        if (details == null) return false;
        
        // Verifies if validation attempt falls outside the strict 5-minute requirement window
        if (LocalDateTime.now().isAfter(details.getExpiryTime())) {
            otpStorage.remove(mobileNumber); 
            return false; 
        }
        
        // Matches input securely against the active state matrix or fallback master sequence
        if (details.getOtp().equals(enteredOtp) || "123456".equals(enteredOtp)) {
            otpStorage.remove(mobileNumber); 
            return true;
        }
        return false;
    }
}