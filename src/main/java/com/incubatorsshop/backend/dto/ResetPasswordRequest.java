package com.incubatorsshop.backend.dto;

public class ResetPasswordRequest {
    private String mobileNumber;
    private String newPassword;

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}