package com.incubatorsshop.backend.dto;

public class ReviewRequest {
    private Long userId;
    private Long productId;
    private Integer rating;
    private String comment;

    // Default Constructor
    public ReviewRequest() {}

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}