package com.incubatorsshop.backend.dto;

public class RevenueAnalyticsResponse {
    private Long productId;
    private String productDescription;
    private int totalUnitsSold;
    private double grossRevenue;
    private boolean isHighDemand;

    public RevenueAnalyticsResponse() {
    }

    public RevenueAnalyticsResponse(Long productId, String productDescription, int totalUnitsSold, double grossRevenue) {
        this.productId = productId;
        this.productDescription = productDescription;
        this.totalUnitsSold = totalUnitsSold;
        this.grossRevenue = grossRevenue;
        this.isHighDemand = false; // Default to false until calculated
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public int getTotalUnitsSold() { return totalUnitsSold; }
    public void setTotalUnitsSold(int totalUnitsSold) { this.totalUnitsSold = totalUnitsSold; }

    public double getGrossRevenue() { return grossRevenue; }
    public void setGrossRevenue(double grossRevenue) { this.grossRevenue = grossRevenue; }

    public boolean isHighDemand() { return isHighDemand; }
    public void setHighDemand(boolean highDemand) { this.isHighDemand = highDemand; }
}