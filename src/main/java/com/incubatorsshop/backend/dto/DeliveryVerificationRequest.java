package com.incubatorsshop.backend.dto;

public class DeliveryVerificationRequest {
    private String enteredOtp;

    // Must have Getter and Setter
    public String getEnteredOtp() {
        return enteredOtp;
    }

    public void setEnteredOtp(String enteredOtp) {
        this.enteredOtp = enteredOtp;
    }
}