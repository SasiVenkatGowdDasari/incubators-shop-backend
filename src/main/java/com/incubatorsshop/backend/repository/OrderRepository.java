package com.incubatorsshop.backend.repository;

import com.incubatorsshop.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Existing method to find orders by user
    List<Order> findByUserId(Long userId);

    // --- NEW: ANALYTICS QUERIES ---

    // 1. Calculate Total Revenue (Only sum orders that are successfully placed, accepted, transit, or delivered)
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status IN ('PLACED', 'ACCEPTED', 'IN_TRANSIT', 'DELIVERED')")
    Double calculateTotalRevenue();

    // 2. Count Pending Orders (Orders waiting for Admin to click 'Accept')
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PLACED'")
    Long countPendingOrders();
 // 3. Aggregate Revenue by Date Range for the Chart
    @Query(value = "SELECT DATE(order_date) as date, SUM(total_price) as revenue " +
                   "FROM orders " +
                   "WHERE status IN ('PLACED', 'ACCEPTED', 'IN_TRANSIT', 'DELIVERED') " +
                   "AND order_date >= :startDate AND order_date <= :endDate " +
                   "GROUP BY DATE(order_date) " +
                   "ORDER BY DATE(order_date)", 
           nativeQuery = true)
    List<Object[]> getRevenueByDateRange(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    List<Order> findByProductId(Long productId);

}