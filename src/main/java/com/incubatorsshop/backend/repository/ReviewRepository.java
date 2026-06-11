package com.incubatorsshop.backend.repository;

import com.incubatorsshop.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Fetches all reviews for a specific product and sorts them so the newest are at the top!
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
}