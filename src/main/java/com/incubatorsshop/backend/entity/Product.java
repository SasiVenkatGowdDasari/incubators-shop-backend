package com.incubatorsshop.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "actual_price", nullable = false)
    private double actualPrice;

    @Column(name = "current_price", nullable = false)
    private double currentPrice;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(columnDefinition = "TEXT")
    private String description; // Changed from longDescription

    @Column(name = "material")
    private String material;

    @Column(name = "type")
    private String type;

    @Column(name = "capacity")
    private String capacity;

    @Column(name = "warranty")
    private String warranty;

    @Column(name = "shipping_options")
    private String shippingOptions;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Transient
    private Double rating = 0.0;

    @Transient
    private Integer totalPurchases = 0;

    public Product() {}
    
    public Product(Long id, String title, double actualPrice, double currentPrice, int stockQuantity,
            String imageUrl, String videoUrl, String description, String material, String type, String capacity,
            String warranty, String shippingOptions, Double rating, Integer totalPurchases,boolean isActive) {
        this.id = id;
        this.title = title;
        this.actualPrice = actualPrice;
        this.currentPrice = currentPrice;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.description = description;
        this.material = material;
        this.type = type;
        this.capacity = capacity;
        this.warranty = warranty;
        this.shippingOptions = shippingOptions;
        this.rating = rating;
        this.totalPurchases = totalPurchases;
        this.isActive=isActive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public double getActualPrice() { return actualPrice; }
    public void setActualPrice(double actualPrice) { this.actualPrice = actualPrice; }
    
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getCapacity() { return capacity; }
    public void setCapacity(String capacity) { this.capacity = capacity; }
    
    public String getWarranty() { return warranty; }
    public void setWarranty(String warranty) { this.warranty = warranty; }
    
    public String getShippingOptions() { return shippingOptions; }
    public void setShippingOptions(String shippingOptions) { this.shippingOptions = shippingOptions; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Integer getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(Integer totalPurchases) { this.totalPurchases = totalPurchases; }
}