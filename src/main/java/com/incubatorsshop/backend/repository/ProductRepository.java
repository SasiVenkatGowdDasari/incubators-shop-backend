package com.incubatorsshop.backend.repository;

import com.incubatorsshop.backend.entity.Product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // --- NEW: ANALYTICS QUERY ---
    
    // Count items where stock is dangerously low (less than 5)
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity < 5")
    Long countLowStockItems();
    
    List<Product> findByIsActiveTrue();
    
}