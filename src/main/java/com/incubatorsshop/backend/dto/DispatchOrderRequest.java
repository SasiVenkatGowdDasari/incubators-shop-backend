package com.incubatorsshop.backend.dto;

public class DispatchOrderRequest {
    private String dispatchOtp;

    // Must have Getter and Setter
    public String getDispatchOtp() {
        return dispatchOtp;
    }

    public void setDispatchOtp(String dispatchOtp) {
        this.dispatchOtp = dispatchOtp;
    }
}