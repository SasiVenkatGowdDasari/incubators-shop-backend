package com.incubatorsshop.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "total_price", nullable = false)
	private double totalPrice;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime orderDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "status", length = 20)
	private OrderStatus status;

	@Column(name = "delivery_otp", nullable = true)
	private String deliveryOtp;

	@Column(name = "user_reviewed", nullable = false)
	private boolean userReviewed = false;

	@Column(name = "admin_reviewed", nullable = false)
	private boolean adminReviewed = false;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	@Column(name = "delivered_date")
	private LocalDateTime deliveredDate;

	public Order() {
	}

	public Order(Long id, User user, Product product, int quantity, double totalPrice, LocalDateTime orderDate,
			OrderStatus status, String deliveryOtp) {
		this.id = id;
		this.user = user;
		this.product = product;
		this.quantity = quantity;
		this.totalPrice = totalPrice;
		this.orderDate = orderDate;
		this.status = status;
		this.deliveryOtp = deliveryOtp;
	}

	// Existing Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public String getDeliveryOtp() {
		return deliveryOtp;
	}

	public void setDeliveryOtp(String deliveryOtp) {
		this.deliveryOtp = deliveryOtp;
	}

	public boolean isUserReviewed() {
		return userReviewed;
	}

	public void setUserReviewed(boolean userReviewed) {
		this.userReviewed = userReviewed;
	}

	public boolean isAdminReviewed() {
		return adminReviewed;
	}

	public void setAdminReviewed(boolean adminReviewed) {
		this.adminReviewed = adminReviewed;
	}

	public LocalDateTime getDeliveredDate() {
		return deliveredDate;
	}

	public void setDeliveredDate(LocalDateTime deliveredDate) {
		this.deliveredDate = deliveredDate;
	}
}