package com.incubatorsshop.backend.dto;

public class OtpVerificationRequest {
    private String mobileNumber;
    private String otpCode;

    public OtpVerificationRequest() {
    }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}