package com.incubatorsshop.backend.controller;

import com.incubatorsshop.backend.dto.DashboardStatsDTO;
import com.incubatorsshop.backend.entity.Product;
import com.incubatorsshop.backend.repository.OrderRepository;
import com.incubatorsshop.backend.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
//@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public AdminController(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        Double revenue = orderRepository.calculateTotalRevenue();
        Long totalOrders = orderRepository.count(); // Built-in JPA method
        Long pendingOrders = orderRepository.countPendingOrders();
        
        // --- UPDATED: Only count active products for low stock ---
        List<Product> allProducts = productRepository.findAll();
        Long lowStock = allProducts.stream()
                .filter(Product::isActive) // Ignore hidden products
                .filter(p -> p.getStockQuantity() < 5) // Count if stock is less than 5
                .count();

        DashboardStatsDTO stats = new DashboardStatsDTO(revenue, totalOrders, pendingOrders, lowStock);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/chart-data")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getChartData(
            @org.springframework.web.bind.annotation.RequestParam("startDate") String startDateStr,
            @org.springframework.web.bind.annotation.RequestParam("endDate") String endDateStr) {
        
        java.time.LocalDateTime start = java.time.LocalDate.parse(startDateStr).atStartOfDay();
        java.time.LocalDateTime end = java.time.LocalDate.parse(endDateStr).atTime(23, 59, 59);

        java.util.List<Object[]> results = orderRepository.getRevenueByDateRange(start, end);
        java.util.List<java.util.Map<String, Object>> chartData = new java.util.ArrayList<>();
        
        for (Object[] row : results) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            // row[0] is the Date, row[1] is the SUM(revenue)
            map.put("name", row[0].toString()); 
            map.put("revenue", row[1]);
            chartData.add(map);
        }

        return ResponseEntity.ok(chartData);
    }
}