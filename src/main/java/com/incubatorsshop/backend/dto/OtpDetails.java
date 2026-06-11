package com.incubatorsshop.backend.dto;

import java.time.LocalDateTime;

public class OtpDetails {
    private String otp;
    private LocalDateTime expiryTime;

    public OtpDetails(String otp, LocalDateTime expiryTime) {
        this.otp = otp;
        this.expiryTime = expiryTime;
    }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
}