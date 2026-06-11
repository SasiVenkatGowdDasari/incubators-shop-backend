package com.incubatorsshop.backend.repository;

import com.incubatorsshop.backend.entity.ReviewMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewMediaRepository extends JpaRepository<ReviewMedia, Long> {
    
    // Fetches all attached images or videos for a specific review
    List<ReviewMedia> findByReviewId(Long reviewId);
}