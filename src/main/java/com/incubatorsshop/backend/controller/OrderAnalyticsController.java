package com.incubatorsshop.backend.controller;

import com.incubatorsshop.backend.dto.RevenueAnalyticsResponse;
import com.incubatorsshop.backend.entity.Order;
import com.incubatorsshop.backend.entity.OrderStatus;
import com.incubatorsshop.backend.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = "*")
public class OrderAnalyticsController {

    private final OrderRepository orderRepository;

    public OrderAnalyticsController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueAnalyticsResponse>> getRevenueMetrics() {
        List<Order> orders = orderRepository.findAll();
        Map<Long, RevenueAnalyticsResponse> calculationMap = new HashMap<>();

        for (Order order : orders) {
            Long productId = order.getProduct().getId();
            String description = order.getProduct().getTitle();
            
            RevenueAnalyticsResponse entry = calculationMap.getOrDefault(productId, 
                new RevenueAnalyticsResponse(productId, description, 0, 0.0));

            entry.setTotalUnitsSold(entry.getTotalUnitsSold() + order.getQuantity());
            
            // Only count DELIVERED items as finalized gross revenue
            if (order.getStatus() == OrderStatus.DELIVERED) {
                entry.setGrossRevenue(entry.getGrossRevenue() + order.getTotalPrice());
            }

            calculationMap.put(productId, entry);
        }

        List<RevenueAnalyticsResponse> outputList = new ArrayList<>(calculationMap.values());
        
        if (!outputList.isEmpty()) {
            // Sort by highest units sold
            outputList.sort((a, b) -> Integer.compare(b.getTotalUnitsSold(), a.getTotalUnitsSold()));
            // Top selling product gets the High Demand badge
            outputList.getFirst().setHighDemand(true);
        }

        return ResponseEntity.ok(outputList);
    }
}