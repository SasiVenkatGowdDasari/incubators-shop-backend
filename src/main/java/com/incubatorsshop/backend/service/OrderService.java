package com.incubatorsshop.backend.service;

import com.incubatorsshop.backend.entity.Order;
import com.incubatorsshop.backend.entity.OrderStatus;
import com.incubatorsshop.backend.entity.Product;
import com.incubatorsshop.backend.entity.User;
import com.incubatorsshop.backend.repository.OrderRepository;
import com.incubatorsshop.backend.repository.ProductRepository;
import com.incubatorsshop.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;

	public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
			UserRepository userRepository, EmailService emailService) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
		this.userRepository = userRepository;
		this.emailService = emailService;
	}

	public Order placeOrder(Long userId, Long productId, int quantity) throws Exception {
		User user = userRepository.findById(userId).orElseThrow(() -> new Exception("User not found"));
		Product product = productRepository.findById(productId).orElseThrow(() -> new Exception("Product not found"));

		if (product.getStockQuantity() < quantity) {
			throw new Exception("Insufficient stock available.");
		}

		// Calculate totals and reduce stock
		double totalPrice = product.getCurrentPrice() * quantity;
		product.setStockQuantity(product.getStockQuantity() - quantity);
		productRepository.save(product);

		// Build and save the order using the OBJECTS, not the IDs
		Order order = new Order();
		order.setUser(user);
		order.setProduct(product);
		order.setQuantity(quantity);
		order.setTotalPrice(totalPrice);
		order.setOrderDate(LocalDateTime.now());
		order.setStatus(OrderStatus.PLACED);
		order.setDeliveryOtp(null);

		Order savedOrder = orderRepository.save(order);

		// --- NEW: Trigger Emails to BOTH Admin and Customer ---
		try {
			emailService.sendOrderNotificationToAdmin(savedOrder);
			emailService.sendOrderConfirmationToCustomer(savedOrder);
		} catch (Exception e) {
			// Catching exceptions here ensures that even if an email fails to send
			// (e.g. bad internet connection), the user's order is still placed
			// successfully.
			System.err.println("Non-fatal error: Failed to send email notifications - " + e.getMessage());
		}

		return savedOrder;
	}

	public void dispatchOrder(Long orderId, String dispatchOtp) throws Exception {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new Exception("Order not found"));

		order.setStatus(OrderStatus.IN_TRANSIT);
		order.setDeliveryOtp(dispatchOtp);
		Order updatedOrder = orderRepository.save(order);

		// --- NEW: Trigger Email Notification (In Transit + OTP) ---
		emailService.sendOrderStatusEmail(updatedOrder);
	}

	public void confirmDelivery(Long orderId, String enteredOtp) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

		if (order.getDeliveryOtp() == null || enteredOtp == null
				|| !order.getDeliveryOtp().trim().equals(enteredOtp.trim())) {
			throw new RuntimeException("Invalid Delivery OTP.");
		}

		order.setStatus(OrderStatus.DELIVERED);
		order.setDeliveredDate(LocalDateTime.now());
		Order updatedOrder = orderRepository.save(order);

		// --- NEW: Trigger Email Notification (Delivered) ---
		emailService.sendOrderStatusEmail(updatedOrder);
	}
}