package com.incubatorsshop.backend.controller;

import com.incubatorsshop.backend.dto.DeliveryVerificationRequest;
import com.incubatorsshop.backend.dto.DispatchOrderRequest;
import com.incubatorsshop.backend.entity.Order;
import com.incubatorsshop.backend.repository.OrderRepository;
import com.incubatorsshop.backend.service.EmailService;
import com.incubatorsshop.backend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // Updated for deployment
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    
    public OrderController(OrderService orderService, OrderRepository orderRepository, EmailService emailService) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }
    
    // --- Inner DTOs to catch the React Cart JSON Array ---
    public static class OrderPayload {
        public Long userId;
        public Double totalAmount;
        public List<ItemPayload> items;
    }

    public static class ItemPayload {
        public Long productId;
        public Integer quantity;
        public Double price;
    }

    // CUSTOMER: Place a new order
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderPayload payload) {
        try {
            for (ItemPayload item : payload.items) {
                // Calls the 3-argument method from your updated service
                orderService.placeOrder(payload.userId, item.productId, item.quantity);
            }
            return ResponseEntity.ok("Order placed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // CUSTOMER: Fetch My Orders
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        List<Map<String, Object>> response = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("totalAmount", order.getTotalPrice());
            map.put("status", order.getStatus().name());
            map.put("orderDate", order.getOrderDate());
            map.put("productId", order.getProduct() != null ? order.getProduct().getId() : null);
            map.put("userReviewed", order.isUserReviewed());
            map.put("adminReviewed", order.isAdminReviewed());
            map.put("quantity", order.getQuantity());
            map.put("deliveredDate", order.getDeliveredDate());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }
        
    // ADMIN: Fetch All Orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    // ADMIN: Dispatch Order
    @PutMapping("/{orderId}/dispatch")
    public ResponseEntity<String> dispatchOrder(@PathVariable Long orderId, @RequestBody DispatchOrderRequest request) {
        try {
            orderService.dispatchOrder(orderId, request.getDispatchOtp());
            return ResponseEntity.ok("Order dispatched. Delivery OTP securely assigned.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // CUSTOMER: Verify Delivery
    @PostMapping("/{orderId}/verify-delivery")
    public ResponseEntity<String> verifyDelivery(@PathVariable Long orderId, @RequestBody DeliveryVerificationRequest request) {
        try {
            orderService.confirmDelivery(orderId, request.getEnteredOtp());
            return ResponseEntity.ok("Delivery Confirmed. Status updated to DELIVERED.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        try {
            String incomingStatus = payload.get("status").toUpperCase().trim();
            if (incomingStatus.equals("CANCELED")) {
                incomingStatus = "CANCELLED";
            }

            java.util.Optional<com.incubatorsshop.backend.entity.Order> optionalOrder = orderRepository.findById(id);
            if (optionalOrder.isEmpty()) {
                return ResponseEntity.status(404).body("Order not found");
            }
            
            com.incubatorsshop.backend.entity.Order order = optionalOrder.get();
            order.setStatus(com.incubatorsshop.backend.entity.OrderStatus.valueOf(incomingStatus)); 
            
            if (incomingStatus.equals("DELIVERED")) {
                order.setDeliveredDate(java.time.LocalDateTime.now());
            }
            
            com.incubatorsshop.backend.entity.Order savedOrder = orderRepository.save(order);

            // <-- REPLACED: Removed the manual thread, relying purely on @Async safely
            try {
                emailService.sendOrderStatusEmail(savedOrder);
            } catch (Exception ex) {
                System.err.println("Non-fatal email error: " + ex.getMessage());
            }

            return ResponseEntity.ok(savedOrder);
            
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}