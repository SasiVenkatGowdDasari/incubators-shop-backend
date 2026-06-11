package com.incubatorsshop.backend.dto;

public class DashboardStatsDTO {
    private Double totalRevenue;
    private Long totalOrders;
    private Long pendingOrders;
    private Long lowStockItems;

    public DashboardStatsDTO(Double totalRevenue, Long totalOrders, Long pendingOrders, Long lowStockItems) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
        this.totalOrders = totalOrders != null ? totalOrders : 0L;
        this.pendingOrders = pendingOrders != null ? pendingOrders : 0L;
        this.lowStockItems = lowStockItems != null ? lowStockItems : 0L;
    }

    // Getters and Setters
    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
    public Long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(Long pendingOrders) { this.pendingOrders = pendingOrders; }
    public Long getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(Long lowStockItems) { this.lowStockItems = lowStockItems; }
}